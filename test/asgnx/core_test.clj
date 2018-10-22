(ns asgnx.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [<!!]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check.generators :as gen]
            [asgnx.core :refer :all]
            [asgnx.kvstore :as kvstore :refer [put! get!]]))



(deftest words-test
  (testing "that sentences can be split into their constituent words"
    (is (= ["a" "b" "c"] (words "a b c")))
    (is (= [] (words "   ")))
    (is (= [] (words nil)))
    (is (= ["a"] (words "a")))
    (is (= ["a"] (words "a ")))
    (is (= ["a" "b"] (words "a b")))))


(deftest cmd-test
  (testing "that commands can be parsed from text messages"
    (is (= "foo" (cmd "foo")))
    (is (= "foo" (cmd "foo x y")))
    (is (= nil   (cmd nil)))
    (is (= ""    (cmd "")))))


(deftest args-test
  (testing "that arguments can be parsed from text messages"
    (is (= ["x" "y"] (args "foo x y")))
    (is (= ["x"] (args "foo x")))
    (is (= [] (args "foo")))
    (is (= [] (args nil)))))


(deftest parsed-msg-test
  (testing "that text messages can be parsed into cmd/args data structures"
    (is (= {:cmd "foo"
            :args ["x" "y"]}
           (parsed-msg "foo x y")))
    (is (= {:cmd "foo"
            :args ["x"]}
           (parsed-msg "foo x")))
    (is (= {:cmd "foo"
            :args []}
           (parsed-msg "foo")))
    (is (= {:cmd "foo"
            :args ["x" "y" "z" "somereallylongthing"]}
           (parsed-msg "foo x y z somereallylongthing")))))

(deftest welcome-test
  (testing "that welcome messages are correctly formatted"
    (is (= "Welcome bob" (welcome {:cmd "welcome" :args ["bob"]})))
    (is (= "Welcome bob" (welcome {:cmd "welcome" :args ["bob" "smith"]})))
    (is (= "Welcome bob smith jr" (welcome {:cmd "welcome" :args ["bob smith jr"]})))))


(deftest homepage-test
  (testing "that the homepage is output correctly"
    (is (= cs4278-brightspace (homepage {:cmd "homepage" :args []})))))


(deftest format-hour-test
  (testing "that 0-23 hour times are converted to am/pm correctly"
    (is (= "1am" (format-hour 1)))
    (is (= "1pm" (format-hour 13)))
    (is (= "2pm" (format-hour 14)))
    (is (= "12am" (format-hour 0)))
    (is (= "12pm" (format-hour 12)))))


(deftest formatted-hours-test
  (testing "that the office hours data structure is correctly converted to a string"
    (is (= "from 8am to 10am in the chairs outside of the Wondry"
           (formatted-hours {:start 8 :end 10 :location "the chairs outside of the Wondry"})))
    (is (= "from 4am to 2pm in the chairs outside of the Wondry"
           (formatted-hours {:start 4 :end 14 :location "the chairs outside of the Wondry"})))
    (is (= "from 2pm to 10pm in the chairs outside of the Wondry"
           (formatted-hours {:start 14 :end 22 :location "the chairs outside of the Wondry"})))))


(deftest office-hours-for-day-test
  (testing "testing lookup of office hours on a specific day"
    (is (= "from 8am to 10am in the chairs outside of the Wondry"
           (office-hours {:cmd "office hours" :args ["thursday"]})))
    (is (= "from 8am to 10am in the chairs outside of the Wondry"
           (office-hours {:cmd "office hours" :args ["tuesday"]})))
    (is (= "there are no office hours on that day"
           (office-hours {:cmd "office" :args ["wednesday"]})))
    (is (= "there are no office hours on that day"
           (office-hours {:cmd "office" :args ["monday"]})))))


(deftest create-router-test
  (testing "correct creation of a function to lookup a handler for a parsed message"
    (let [router (create-router {"hello" #(str (:cmd %) " " "test")
                                 "argc"  #(count (:args %))
                                 "echo"  identity
                                 "default" (fn [& a] "No!")})
          msg1   {:cmd "hello"}
          msg2   {:cmd "argc" :args [1 2 3]}
          msg3   {:cmd "echo" :args ["a" "z"]}
          msg4   {:cmd "echo2" :args ["a" "z"]}]
      (is (= "hello test" ((router msg1) msg1)))
      (is (= "No!" ((router msg4) msg4)))
      (is (= 3 ((router msg2) msg2)))
      (is (= msg3 ((router msg3) msg3))))))


(deftest action-send-msg-test
  (testing "That action send msg returns a correctly formatted map"
    (is (= :send
           (:action (action-send-msg :bob "foo"))))
    (is (= :bob
           (:to (action-send-msg :bob "foo"))))
    (is (= "foo"
           (:msg (action-send-msg [:a :b] "foo"))))))


(deftest action-send-msgs-test
  (testing "That action send msgs generates a list of sends"
    (let [a (action-send-msg [:a :f :b] 1)
          b (action-send-msg [:a :f :d] 1)
          c (action-send-msg [:a :f :e] 1)
          d (action-send-msg [:a :f :c] 1)]
      (is (= [a b c d]
             (action-send-msgs [[:a :f :b]
                                [:a :f :d]
                                [:a :f :e]
                                [:a :f :c]]
                              1))))))

(deftest action-insert-test
  (testing "That action insert returns a correctly formatted map"
    (is (= #{:action :ks :v}
           (into #{}(keys (action-insert [:a :b] {:foo 1})))))
    (is (= #{:assoc-in [:a :b] {:foo 1}}
           (into #{}(vals (action-insert [:a :b] {:foo 1})))))
    (is (= :assoc-in
           (:action (action-insert [:a :b] {:foo 1}))))
    (is (= {:foo 1}
           (:v (action-insert [:a :b] {:foo 1}))))
    (is (= [:a :b]
           (:ks (action-insert [:a :b] {:foo 1}))))))


(deftest action-remove-test
  (testing "That action remove returns a correctly formatted map"
    (is (= #{:action :ks}
         (into #{} (keys (action-remove [:a :b])))))
    (is (= #{:dissoc-in [:a :b]}
          (into #{}(vals (action-remove [:a :b])))))
    (is (= :dissoc-in
           (:action (action-remove [:a :b]))))
    (is (= [:a :b]
           (:ks (action-remove [:a :b]))))))


(deftest action-inserts-test
  (testing "That action inserts generates a list of inserts"
    (let [a (action-insert [:a :f :b] 1)
          b (action-insert [:a :f :d] 1)
          c (action-insert [:a :f :e] 1)
          d (action-insert [:a :f :c] 1)]
      (is (= [a b c d]
             (action-inserts [:a :f] [:b :d :e :c] 1))))))


(defn action-send [system {:keys [to msg]}]
  (put! (:state-mgr system) [:msgs to] msg))

(defn pending-send-msgs [system to]
  (get! (:state-mgr system) [:msgs to]))

(def send-action-handlers
  {:send action-send})

(deftest handle-message-test
  (testing "the integration and handling of messages"
    (let [ehdlrs (merge
                   send-action-handlers
                   kvstore/action-handlers)
          state  (atom {})
          smgr   (kvstore/create state)
          system {:state-mgr smgr
                  :effect-handlers ehdlrs}]
      (is (= "There are no experts on that topic."
             (<!! (handle-message
                    system
                    "test-user"
                    "ask food best burger in nashville"))))
      (is (= "test-user is now an expert on food."
             (<!! (handle-message
                    system
                    "test-user"
                    "expert food"))))
      (is (= "Asking 1 expert(s) for an answer to: \"what burger\""
             (<!! (handle-message
                    system
                    "test-user"
                    "ask food what burger"))))
      (is (= "what burger"
             (<!! (pending-send-msgs system "test-user"))))
      (is (= "test-user2 is now an expert on food."
             (<!! (handle-message
                    system
                    "test-user2"
                    "expert food"))))
      (is (= "Asking 2 expert(s) for an answer to: \"what burger\""
             (<!! (handle-message
                    system
                    "test-user"
                    "ask food what burger"))))
      (is (= "what burger"
             (<!! (pending-send-msgs system "test-user"))))
      (is (= "what burger"
             (<!! (pending-send-msgs system "test-user2"))))
      (is (= "You must ask a valid question."
             (<!! (handle-message
                    system
                    "test-user"
                    "ask food "))))
      (is (= "test-user is now an expert on nashville."
             (<!! (handle-message
                    system
                    "test-user"
                    "expert nashville"))))
      (is (= "Asking 1 expert(s) for an answer to: \"what bus\""
             (<!! (handle-message
                    system
                    "test-user2"
                    "ask nashville what bus"))))
      (is (= "what bus"
             (<!! (pending-send-msgs system "test-user"))))
      (is (= "Your answer was sent."
             (<!! (handle-message
                   system
                   "test-user"
                   "answer the blue bus"))))
      (is (= "the blue bus"
             (<!! (pending-send-msgs system "test-user2"))))
      (is (= "You did not provide an answer."
             (<!! (handle-message
                   system
                   "test-user"
                   "answer"))))
      (is (= "You haven't been asked a question."
             (<!! (handle-message
                   system
                   "test-user3"
                   "answer the blue bus")))))))



;; Tests for sending and receiving announcements for courses
;; for assignment 5.

;; Test for register-instructor function
(deftest register-instructor-test
  (testing "Return action to register instructor for a course"
    (is (= [(action-insert ["cs5278" :instructor "professor1"] "info")]
           (instructor-register {} "cs5278" "professor1" "info")))))

;; Test for register-students function
(deftest register-students-test
  (testing "Return action to register students for a course"
    (is (= [(action-insert [:students "cs5278" 12345] "info")]
           (students-register {} "cs5278" 12345 "info")))))

;; Integration testing for sending and receiving announcements.
(deftest handle-message-test
  (testing "the integration and handling of messages"
    (let [ehdlrs (merge
                   send-action-handlers
                   kvstore/action-handlers)
          state  (atom {})
          smgr   (kvstore/create state)
          system {:state-mgr smgr
                  :effect-handlers ehdlrs}]
      ; If a user is not the instructor for a course and sends an invalid
      ; message, the user should receive a message telling them they are
      ; not the instructor.
      ; (is (= "You are not the instructor for cs5278."
      ;        (<!! (handle-message
      ;               system
      ;               "cs5278instructor"
      ;               "announcement cs5278"))))
      ; ; Test that user who is not the instructor for a course cannot
      ; ; send a message.
      ; (is (= "You are not the instructor for cs5278."
      ;        (<!! (handle-message
      ;               system
      ;               "cs5278instructor"
      ;               "announcement cs5278 test tomorrow"))))
      ; Test that instructor can successfully be added for a course.
      (is (= "cs5278instructor is now the instructor for cs5278."
             (<!! (handle-message
                    system
                    "cs5278instructor"
                    "instructor cs5278"))))
      ; Test that instructor sending announcement to a course without
      ; registered students will receive an error message.
      (is (= "There are no students enrolled in cs5278."
             (<!! (handle-message
                    system
                    "cs5278instructor"
                    "announcement cs5278 test tomorrow"))))
      ; Test that student can be added to a course.
      (is (= "student1 is now a student in cs5278."
             (<!! (handle-message
                    system
                    "student1"
                    "student cs5278"))))
      ; Test that an instructor sending an empty announcement to a course
      ; with a student will receive an error message.
      (is (= "You have sent an empty announcement."
             (<!! (handle-message
                    system
                    "cs5278instructor"
                    "announcement cs5278"))))
      ; Test that an instructor is able to send a valid announcement to
      ; a student.
      (is (= "You have sent an announcement to cs5278."
             (<!! (handle-message
                    system
                    "cs5278instructor"
                    "announcement cs5278 test tomorrow"))))
      (is (= "cs5278 announcement: test tomorrow"
             (<!! (pending-send-msgs system "student1"))))
      ; Test that only one instructor can be registered for a course and
      ; send announcements.
      ; (is (= "There is already an instructor for cs5278."
      ;        (<!! (handle-message
      ;               system
      ;               "student2"
      ;               "instructor cs5278"))))
      ; (is (= "You are not the instructor for cs5278."
      ;        (<!! (handle-message
      ;               system
      ;               "student2"
      ;               "announcement cs5278 test cancelled tomorrow!!!!!"))))
      ; Test for sending messages to two students.
      (is (= "professor2 is now the instructor for math1301."
             (<!! (handle-message
                    system
                    "professor2"
                    "instructor math1301"))))
      (is (= "student2 is now a student in math1301."
             (<!! (handle-message
                    system
                    "student2"
                    "student math1301"))))
      (is (= "student3 is now a student in math1301."
             (<!! (handle-message
                    system
                    "student3"
                    "student math1301"))))
      (is (= "You have sent an announcement to math1301."
             (<!! (handle-message
                    system
                    "professor2"
                    "announcement math1301 homework due tomorrow"))))
      (is (= "math1301 announcement: homework due tomorrow"
             (<!! (pending-send-msgs system "student2"))))
      (is (= "math1301 announcement: homework due tomorrow"
             (<!! (pending-send-msgs system "student3")))))))
      ; (is (= "You are not the instructor for math1301."
      ;        (<!! (handle-message
      ;               system
      ;               "student2"
      ;               "announcement math1301 homework postponed to next week")))))))
