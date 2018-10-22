(ns asgnx.core
  (:require [clojure.string :as string]
            [clojure.core.async :as async :refer [go chan <! >!]]
            [asgnx.kvstore :as kvstore
             :refer [put! get! list! remove!]]))


;; Do not edit!
;; A def for the course home page URL.
(def cs4278-brightspace "https://brightspace.vanderbilt.edu/d2l/home/85892")


;; Do not edit!
;; A map specifying the instructor's office hours that is keyed by day of the week.
(def instructor-hours {"tuesday"  {:start    8
                                   :end      10
                                   :location "the chairs outside of the Wondry"}

                       "thursday" {:start    8
                                   :end      10
                                   :location "the chairs outside of the Wondry"}})


;; This is a helper function that you might want to use to implement
;; `cmd` and `args`.
(defn words [msg]
  (if msg
      (string/split msg #" ")
      []))

;; Asgn 1.
;;
;; @Todo: Fill in this function to return the first word in a text
;; message.
;;
;; Example: (cmd "foo bar") => "foo"
;;
;; See the cmd-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn cmd [msg]
  (first (words msg)))

;; Asgn 1.
;;
;; @Todo: Fill in this function to return the list of words following
;; the command in a text message.
;;
;; Example: (args "foo bar baz") => ("bar" "baz")
;;
;; See the args-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn args [msg]
  (rest (words msg)))

;; Asgn 1.
;;
;; @Todo: Fill in this function to return a map with keys for the
;; :cmd and :args parsed from the msg.
;;
;; Example:
;;
;; (parsed-msg "foo bar baz") => {:cmd "foo" :args ["bar" "baz"]}
;;
;; See the parsed-msg-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn parsed-msg [msg]
  {:cmd (cmd msg) :args (args msg)})

;; Asgn 1.
;;
;; @Todo: Fill in this function to prefix the first of the args
;; in a parsed message with "Welcome " and return the result.
;;
;; Example:
;;
;; (welcome {:cmd "welcome" :args ["foo"]}) => "Welcome foo"
;;
;; See the welcome-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn welcome [pmsg]
  (str "Welcome " (first (:args pmsg))))

;; Asgn 1.
;;
;; @Todo: Fill in this function to return the CS 4278 home page.
;; Use the `cs4278-brightspace` def to produce the output.
;;
;; See the homepage-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn homepage [_]
  (str cs4278-brightspace))

;; Asgn 1.
;;
;; @Todo: Fill in this function to convert from 0-23hr format
;; to AM/PM format.
;;
;; Example: (format-hour 14) => "2pm"
;;
;; See the format-hour-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn format-hour [h]
  (cond
    ;; Special case for midnight
    (= 0 h) "12am"
    ;; Special case for noon
    (= 12 h) "12pm"
    ;; Check for am
    (< h 12) (str h "am")
    ;; Else 12<h<24, so pm
    :else (str (- h 12) "pm")))


;; Asgn 1.
;;
;; @Todo: This function should take a map in the format of
;; the values in the `instructor-hours` map (e.g. {:start ... :end ... :location ...})
;; and convert it to a string format.
;;
;; Example:
;; (formatted-hours {:start 8 :end 10 :location "the chairs outside of the Wondry"}))
;; "from 8am to 10am in the chairs outside of the Wondry"
;;
;; You should use your format-hour function to implement this.
;;
;; See the formatted-hours-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn formatted-hours [hours]
  ;; Use format hours to format the start and end times from the map
  (str "from " (format-hour (:start hours)) " to "
       (format-hour (:end hours)) " in " (:location hours)))

;; Asgn 1.
;;
;; @Todo: This function should lookup and see if the instructor
;; has office hours on the day specified by the first of the `args`
;; in the parsed message. If so, the function should return the
;; `formatted-hours` representation of the office hours. If not,
;; the function should return "there are no office hours on that day".
;; The office hours for the instructor should be obtained from the
;; `instructor-hours` map.
;;
;; You should use your formatted-hours function to implement this.
;;
;; See the office-hours-for-day-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn office-hours [{:keys [args cmd]}]
  ;; Check if instructor has office hours that day
  (if (contains? instructor-hours (first args))
    ;; If there are office hours, get the office hours using the map and format
    ;; them using formatted-hours
    (formatted-hours(get instructor-hours (first args)))
    ;; If there are no office hours, return a message stating that
    "there are no office hours on that day"))

;; Asgn 2.
;;
;; @Todo: Create a function called action-send-msg that takes
;; a destination for the msg in a parameter called `to`
;; and the message in a parameter called `msg` and returns
;; a map with the keys :to and :msg bound to each parameter.
;; The map should also have the key :action bound to the value
;; :send.
;;
(defn action-send-msg [to msg]
  {:action :send :to to :msg msg})

;; Asgn 2.
;;
;; @Todo: Create a function called action-send-msgs that takes
;; takes a list of people to receive a message in a `people`
;; parameter and a message to send them in a `msg` parmaeter
;; and returns a list produced by invoking the above `action-send-msg`
;; function on each person in the people list.
;;
;; java-like pseudo code:
;;
;; output = new list
;; for person in people:
;;   output.add( action-send-msg(person, msg) )
;; return output
;;
(defn action-send-msgs [people msg]
  ; Use repeat to send the same message, and use map
  ; to map action-send-msg to all people in people list
  (map action-send-msg people (repeat msg)))

;; Asgn 2.
;;
;; @Todo: Create a function called action-insert that takes
;; a list of keys in a `ks` parameter, a value to bind to that
;; key path to in a `v` parameter, and returns a map with
;; the key :ks bound to the `ks` parameter value and the key :v
;; vound to the `v` parameter value.)
;; The map should also have the key :action bound to the value
;; :assoc-in.
;;
(defn action-insert [ks v]
  {:action :assoc-in :ks ks :v v})

;; Asgn 2.
;;
;; @Todo: Create a function called action-inserts that takes:
;; 1. a key prefix (e.g., [:a :b])
;; 2. a list of suffixes for the key (e.g., [:c :d])
;; 3. a value to bind
;;
;; and calls (action-insert combined-key value) for each possible
;; combined-key that can be produced by appending one of the suffixes
;; to the prefix.
;;
;; In other words, this invocation:
;;
;; (action-inserts [:foo :bar] [:a :b :c] 32)
;;
;; would be equivalent to this:
;;
;; [(action-insert [:foo :bar :a] 32)
;;  (action-insert [:foo :bar :b] 32)
;;  (action-insert [:foo :bar :c] 32)]
;;
(defn action-inserts [prefix ks v]
  ; Inner map creates a vectors that have prefix and last element of ks.
  ; Outer map calls action-insert with each of those new vectors and
  ; the parameter v.
  (map action-insert (map conj (repeat prefix) ks) (repeat v)))

;; Asgn 2.
;;
;; @Todo: Create a function called action-remove that takes
;; a list of keys in a `ks` parameter and returns a map with
;; the key :ks bound to the `ks` parameter value.
;; The map should also have the key :action bound to the value
;; :dissoc-in.
;;
(defn action-remove [ks]
  {:action :dissoc-in :ks ks})

;; Asgn 3.
;;
;; @Todo: Create a function called "experts-register"
;; that takes the current application `state`, a `topic`
;; the expert's `id` (e.g., unique name), and information
;; about the expert (`info`) and registers them as an expert on
;; the specified topic. Look at the associated test to see the
;; expected function signature.
;;
;; Your function should NOT directly change the application state
;; to register them but should instead return a list of the
;; approp riate side-effects (above) to make the registration
;; happen.
;;
;; See the integration test in See handle-message-test for the
;; expectations on how your code operates
;;
(defn experts-register [experts topic id info]
    ; Return action to insert all experts
    [(action-insert [:expert topic id] info)])




;; Asgn 3.
;;
;; @Todo: Create a function called "experts-unregister"
;; that takes the current application `state`, a `topic`
;; and the expert's `id` (e.g., unique name) and then
;; removes the expert from the list of experts on that topic.
;; Look at the associated test to see the expected function signature.
;;
;; Your function should NOT directly change the application state
;; to unregister them but should instead return a list of the
;; appropriate side-effects (above) to make the registration
;; happen.
;;
;; See the integration test in See handle-message-test for the
;; expectations on how your code operates
;;
(defn experts-unregister [experts topic id]
  [action-remove [:expert topic id]])

(defn experts-question-msg [experts question-words]
  (str "Asking " (count experts) " expert(s) for an answer to: \""
       (string/join " " question-words) "\""))

;; Asgn 3.
;;
;; @Todo: Create a function called "ask-experts"
;; that takes two parameters:
;;
;; 1. the list of experts on the topic
;; 2. a parsed message with the format:
;;    {:cmd "ask"
;;     :user-id "phone number that sent the message"
;;     :args [topic question-word1 question-word2 ... question-wordN]}
;;
;; The sender of the message will be identified by their phone number
;; in the user-id parameter. This is the phone number that you will need
;; to forward answers to the question to.
;;
;; The parsed message is generated by breaking up the words in the ask
;; text message. For example, if someone sent the message:
;;
;; "ask food what is the best pizza in nashville"
;;
;; The parsed message would be:
;;
;; {:cmd "ask"
;;  :user-id "+15555555555"
;;  :args ["food" "what" "is" "the" "best" "pizza" "in" "nashville"]}
;;
;; This function needs to return a list with two elements:
;; [[actions...] "response to asker"]
;;
;; The actions in the list are the *side effects* that need to take place
;; to ask the question (e.g., sending messages to the experts). The string
;; is the response that is going to be sent back to the person that asked
;; the question (e.g. "Asking 2 expert(s) for an answer to ....").
;;
;; The correct string response to a valid question should be produced with
;; the `experts-question-msg` function above.
;;
;; Think about how you are going to figure out where to route messages
;; when an expert answers (see the conversations query) and make sure you
;; handle the needed side effect for storing the conversation state.
;;
;; If there are no registered experts on a topic, you should return an
;; empty list of actions and "There are no experts on that topic."
;;
;; If there isn't a question, you should return "You must ask a valid question."
;;
;; Why this strange architecture? By returning a list of the actions to take,
;; rather than directly taking that action, we can keep this function pure.
;; Pure functions are WAY easier to test / maintain. Also, we can isolate our
;; messy impure action handling at the "edges" of the application, where it is
;; easier to track and reason about.
;;
;; You should look at `handle-message` to get an idea of the way that this
;; function is going to be used, its expected signature, and how the actions
;; and output are going to work.
;;
;; See the integration test in See handle-message-test for the
;; expectations on how your code operates
;;
(defn ask-experts [experts {:keys [args user-id]}]
  ; Get question as everything except the first word in the parsed
  ; message. Set the action to send messages to all the experts
  ; and the action to insert the expert and user into the
  ; conversations map, and concatenate those two actions together in
  ; a list.
  (let [
        question (string/join " " (rest args))
        message-action (action-send-msgs experts question)
        insert-action(action-inserts [:conversations] experts user-id)
        actions-list (concat message-action insert-action)]
    (cond
      ; Special response if there is no question asked.
      (empty? question) [[](str "You must ask a valid question.")]
      ; Special response for if there are no experts on the topic.
      (nil? experts) [[](str "There are no experts on that topic.")]
      (empty? experts) [[](str "There are no experts on that topic.")]
      ; Otherwise, send messages to the ids of all the experts
      :else [actions-list
              (experts-question-msg experts (rest args))])))



;; Asgn 3.
;;
;; @Todo: Create a function called "answer-question"
;; that takes two parameters:
;;
;; 1. the last conversation describing the last question that was routed
;;    to the expert
;; 2. a parsed message with the format:
;;    {:cmd "ask"
;;     :user-id "+15555555555"
;;     :args [topic answer-word1 answer-word2 ... answer-wordN]}
;;
;; The parsed message is generated by breaking up the words in the ask
;; text message. For example, if someone sent the message:
;;
;; "answer joey's house of pizza"
;;
;; The conversation will be data that you store as a side-effect in
;; ask-experts. You probably want this data to be information about the
;; last question asked to each expert. See the "think about" comment above.
;;
;; The parsed message would be:
;;
;; {:cmd "ask"
;;  :user-id "+15555555555"
;;  :args ["joey's" "house" "of" "pizza"]}
;;
;; This function needs to return a list with two elements:
;; [[actions...] "response to expert answering"]
;;
;; The actions in the list are the *side effects* that need to take place
;; to send the answer to the original question asker. The string
;; is the response that is going to be sent back to the expert answering
;; the question.
;;
;; Think about how you are going to figure out where to route messages
;; when an expert answers (see the conversations query) and make sure you
;; handle the needed side effect for storing the conversation state.
;;
;; If there are no registered experts on a topic, you should return an
;; empty list of actions and "There are no experts on that topic."
;;
;; If there isn't a question, you should return "You must ask a valid question."
;;
;; Why this strange architecture? By returning a list of the actions to take,
;; rather than directly taking that action, we can keep this function pure.
;; Pure functions are WAY easier to test / maintain. Also, we can isolate our
;; messy impure action handling at the "edges" of the application, where it is
;; easier to track and reason about.
;;
;; You should look at `handle-message` to get an idea of the way that this
;; function is going to be used, its expected signature, and how the actions
;; and output are going to work.
;;
;; See the integration test in See handle-message-test for the
;; expectations on how your code operates
;;
(defn answer-question [conversation {:keys [args]}]
  (let [answer (string/join " " args)]
    (cond
      ; If the expert does not have a conversation with anyone,
      ; it means that they have not been asked a question, so return
      ; that as a message to the expert and take no actions.
      (nil? conversation) [[] "You haven't been asked a question."]
      ; If the response by the expert was empty, give them a message
      ; indicating that and take no actions.
      (empty? answer) [[] "You did not provide an answer."]
      ; If the expert sent a valid answer to the a question,
      ; indicate to the expert that the answer was sent and
      ; take the action of sending the message to the person who,
      ; asked the question, which is stored on the conversation
      ; parameter.
      :else [[(action-send-msg
               conversation answer)] "Your answer was sent."])))







;; Asgn 3.
;;
;; @Todo: Create a function called "add-expert"
;; that takes two parameters:
;;
;; 1. the current list of experts on the topic
;; 2. a parsed message with the format:
;;    {:cmd "expert"
;;     :user-id "+15555555555"
;;     :args [topic]
;;
;;
;; The parsed message is generated by breaking up the words in the expert
;; text message. For example, if someone sent the message:
;;
;; "expert food"
;;
;; The parsed message would be:
;;
;; {:cmd "expert"
;;  :user-id "+15555555555"))
;;  :args ["food"]}
;;
;; This function needs to add "sara" to the list of experts on "food" and
;; associate her phone number with her ID.
;;
;; Similar to the function `ask-experts` function, this function needs to
;; return the updated `state`, which should now have the expert registered
;; under the specified topic (e.g., "sara" under "food"). The output to
;; send back to the user should be (str expert-id " is now an expert on " topic)
;;
;; The last line of your function should be something like:
;;
;; [new-state (str expert-id " is now an expert on " topic)]
;;
;; See the integration test in See handle-message-test for the
;; expectations on how your code operates
;;
(defn add-expert [experts {:keys [args user-id]}]
  (let [
        ; Get the topic the user is an expert on
        topic (first args)
        ; New state has expert's user-id inserted into old state
        new-state (experts-register experts topic user-id {})]
    ; Return new state followed by message.
    [new-state (str user-id " is now an expert on " topic ".")]))


;; Assignment 5 functions to implement text message based reminders for
;; courses.

;; Registers the instructor for a course by returning the action
;; necessary to register the instructor.
(defn instructor-register [state course id info]
    ; Return action to insert professor into the correct course
    ; and under the instructor group.
    [(action-insert [:instructor course id] info)])

;; Add a instructor to a specific course by creating a new
;; state using the instructor-register function, and then
;; returning that new state along with a message indicating
;; that the instructor was successfully added to the course.
;; Only one instructor may be registered for a course. If
;; there is already an instructor registered for the course,
;; an error message will be returned.
;;
;; The format of the command for the user to type is: "instructor cs5278"
(defn add-instructor [state {:keys [args user-id]}]
  (let [
        ; Get the course the instructor wants to be added to.
        course (first args)
        ; Get the current instructor for the course.
        current-instructor (get state :instructor)]
    (cond
      ; If there is currently no instructor, register the user
      ; as the current instructor.
      (nil? current-instructor) (let [new-state (instructor-register
                                                 state course user-id {})]
                                  [new-state (str user-id " is now the instructor for "
                                                  course ".")])
      ; If there is already an instructor for the course, return
      ; the current state and an error message.
      :else [state (str "There is already an instructor for " course ".")])))



;; Registers the student for a course by returning the action
;; necessary to register the instructor.
(defn students-register [state course id info]
    ; Return action to insert student into the correct course
    ; and under the students group.
    [(action-insert [:students course id] info)])

;; Add a student to a specific course by creating a new
;; state using the students-register function, and then
;; returning that new state along with a message indicating
;; that the student was successfully added to the course.
;;
;; The format of the command for the user to type is: "student cs5278"
(defn add-student [state {:keys [args user-id]}]
  (let [
        ; Get the course the user wants to register for
        course (first args)
        ; Get new state using the students-register function
        new-state (students-register state course user-id {})]
    ; Return new state followed by message.
    [new-state (str user-id " is now a student in " course ".")]))

;; Function for instructor to send an announcement for a course. Instructor
;; must specify the first word as "announcement" to be routed to this
;; function, and the second word must be the course name to be sent
;; to the correct course.
;; Course-state parameter contains the state for the course, including
;; the current instructor and students.
;;
;; The format of the command for the user to type is:
;; "announcement cs5278 some message here"
(defn send-announcement [students {:keys [args user-id]}]
  (let [
        ; Get the list of students from the course.
        students-list students
        ; Get the instructor for the course.
        ; instructor (first (keys (get course-state :instructor)))
        ; Get the course at the first word in the message.
        course (first args)
        ; Get the announcement content as everything except for the course name.
        announcement-content (string/join " " (rest args))
        ; Create a string for the announcement indicating which
        ; course it is store.
        announcement (str course " announcement: " announcement-content)
        ; Send the message to all the students in the course.
        message-action (action-send-msgs students-list announcement)
        insert-action(action-inserts [:conversations] students-list user-id)
        actions-list (concat message-action insert-action)]
    (cond
      ; Error message if user is not the instructor for a course.
      ; (not (= instructor user-id)) [[] (str "You are not the instructor for "
      ;                                      course ".")]
      ; Special response if announcement content is empty.
      (empty? announcement-content) [[](str "You have sent an empty announcement.")]
      ; Special response for if there are no students.
      (nil? students-list) [[](str "There are no students enrolled in " course ".")]
      (empty? students-list) [[](str "There are no students enrolled in " course ".")]
      ; Otherwise, send messages to all the students.
      :else [actions-list
              (str "You have sent an announcement to " course ".")])))

;; For Lambda Debugging
(defn liststudents [state {:keys [args user-id]}]
  [[](str "Students: " state)])

(defn listinstructors [state {:keys [args user-id]}]
  [[](str "Instructors: " state)])



;; Don't edit!
(defn stateless [f]
  (fn [_ & args]
    [[] (apply f args)]))


;; Router updated for assignment 5 by creating routing for
;; registering a student for a course and having professors
;; send announcements for a course.
(def routes {"default"  (stateless (fn [& args] "Unknown command."))
             "welcome"  (stateless welcome)
             "homepage" (stateless homepage)
             "office"   (stateless office-hours)
             "ask"      ask-experts
             "expert"   add-expert
             "answer"   answer-question
             "student"  add-student
             "instructor" add-instructor
             "announcement" send-announcement
             "liststudents" liststudents
             "listinstructors" listinstructors})


;; Asgn 3.
;;
;; @Todo: Add mappings of the cmds "expert", "ask", and "answer" to
;; to the `routes` map so that the functions that you
;; created will be invoked when the corresponding text message
;; commands are received.
;;})


;; Don't edit!
(defn experts-on-topic-query [state-mgr pmsg]
  (let [[topic]  (:args pmsg)]
    (list! state-mgr [:expert topic])))


;; Don't edit!
(defn conversations-for-user-query [state-mgr pmsg]
  (let [user-id (:user-id pmsg)]
    (get! state-mgr [:conversations user-id])))

; Query for students
(defn student-query [state-mgr pmsg]
  (let [[course]  (:args pmsg)]
    (list! state-mgr [:students course])))

; Query for instructors
(defn instructor-query [state-mgr pmsg]
  (let [[course]  (:args pmsg)]
    (list! state-mgr [:instructors course])))

; Querys for debugging
(defn liststudents-query [state-mgr pmsg]
  (let [[course]  (:args pmsg)]
    (get! state-mgr [:students])))

(defn listinstructors-query [state-mgr pmsg]
  (let [[course]  (:args pmsg)]
    (get! state-mgr [:instructor])))

;; Don't edit!

;; Edited for assignment 5 by getting the state for a course.
(def queries
  {"expert" experts-on-topic-query
   "ask"    experts-on-topic-query
   "answer" conversations-for-user-query
   "student"  student-query
   "announcement" student-query
   "instructor" instructor-query
   "liststudents" liststudents-query
   "listinstructors" listinstructors-query})


;; Don't edit!
(defn read-state [state-mgr pmsg]
  (go
    (if-let [qfn (get queries (:cmd pmsg))]
      (<! (qfn state-mgr pmsg))
      {})))


;; Asgn 1.
;;
;; @Todo: This function should return a function (<== pay attention to the
;; return type) that takes a parsed message as input and returns the
;; function in the `routes` map that is associated with a key matching
;; the `:cmd` in the parsed message. The returned function would return
;; `welcome` if invoked with `{:cmd "welcome"}`.
;;
;; Example:
;;
;; (let [msg {:cmd "welcome" :args ["bob"]}]
;;   (((create-router {"welcome" welcome}) msg) msg) => "Welcome bob"
;;
;; If there isn't a function in the routes map that is mapped to a
;; corresponding key for the command, you should return the function
;; mapped to the key "default".
;;
;; See the create-router-test in test/asgnx/core_test.clj for the
;; complete specification.
;;
(defn create-router [routes]
  ;; Define new function to return.
  (fn [pmsg]
    ;; Check if routes map has function in parsed message.
    (if (contains? routes (:cmd pmsg))
      ;; Return function mapped in routes function if routes map contains the
      ;; function to look for.
      (get routes (:cmd pmsg))
      ;; Otherwise, return the function mapped to default.
      (get routes "default"))))


;; Don't edit!
(defn output [o]
  (second o))


;; Don't edit!
(defn actions [o]
  (first o))


;; Don't edit!
(defn invoke [{:keys [effect-handlers] :as system} e]
  (go
    (println "    Invoke:" e)
    (if-let [action (get effect-handlers (:action e))]
      (do
        (println "    Invoking:" action "with" e)
        (<! (action system e))))))


;; Don't edit!
(defn process-actions [system actions]
  (go
    (println "  Processing actions:" actions)
    (let [results (atom [])]
      (doseq [action actions]
        (let [result (<! (invoke system action))]
          (swap! results conj result)))
      @results)))


;; Don't edit!
(defn handle-message
  "
    This function orchestrates the processing of incoming messages
    and glues all of the pieces of the processing pipeline together.

    The basic flow to handle a message is as follows:

    1. Create the router that will be used later to find the
       function to handle the message
    2. Parse the message
    3. Load any saved state that is going to be needed to process
       the message (e.g., querying the list of experts, etc.)
    4. Find the function that can handle the message
    5. Call the handler function with the state from #3 and
       the message
    6. Run the different actions that the handler returned...these actions
       will be bound to different implementations depending on the environemnt
       (e.g., in test, the actions aren't going to send real text messages)
    7. Return the string response to the message

  "
  [{:keys [state-mgr] :as system} src msg]
  (go
    (println "=========================================")
    (println "  Processing:\"" msg "\" from" src)
    (let [rtr    (create-router routes)
          _      (println "  Router:" rtr)
          pmsg   (assoc (parsed-msg msg) :user-id src)
          _      (println "  Parsed msg:" pmsg)
          state  (<! (read-state state-mgr pmsg))
          _      (println "  Read state:" state)
          hdlr   (rtr pmsg)
          _      (println "  Hdlr:" hdlr)
          [as o] (hdlr state pmsg)
          _      (println "  Hdlr result:" [as o])
          arslt  (<! (process-actions system as))
          _      (println "  Action results:" arslt)]
      (println "=========================================")
      o)))
