# Text message reminders for undergraduate STEM students.

The goal of the project is to help undergraduate STEM students at Vanderbilt, because STEM students here constantly seem stressed out with coursework. From an anecdotal perspective, it seems that students in the humanities often get free extensions for late essays, while STEM students often talk about hard deadlines and a lot of work. The goal is to create an application that will help STEM students get reminders for when their assignments are due since missed deadlines are often so catastrophic for STEM students.


Moreover, I have personally heard of stories of students in Peabody college who will get personal text messages from professors, but have never heard of similar stories with STEM majors. One possible reason may be because STEM professors are concerned about privacy and don't want to give their phone number away. Another reason could be that with so many students in classes, it is very hard to send personal messages to all of them. The existing application framework allows users to anonymously send text messages to a centralized source, and the messages are forwarded to by the system to a group of other users. This seems like it would solve both potential hurdles because students would not be able to see their professors' phone numbers, and because professors could send messages to the entire class at once.


To see if this idea interested any undergraduate STEM majors, I interviewed three STEM majors who are all pre-med students; it seems that pre-med students are more worried about grades and classes than any other group on campus. The questions were mostly open ended in asking what the students wanted to see and what they would be willing to participate in. While the sample size is quite small, it might be possible to infer that many STEM students would be interested in getting text message reminders. Of course, the reminders could be used in any classes; it just seems that some students in Peabody college may not need it because they already communicate with professors via text message.
 


# Questions
1). Do you currently use text messaging for anything other than communicating with contacts?1). Do you currently use text messaging for anything other than communicating with contacts?

2). What would be a feature within a text messaging application that would be useful for you and make you likely to use the application?

3). What do you think are the advantages of text messaging over social media?

4). In what ways do you think Vanderbilt as an entity needs to communicate better with students?

5). How likely would you be willing to test a prototype of a text messaging application?

6). In what ways do you think text messaging could facilitate communication between students and professors in large lectures (if any)?

7). In what ways do you think text messaging could facilitate communication between students and professors outside of the classroom (if any)?

8). In what ways would you like communication between students in the same course improved? Do you think any of it could be done through a text messaging application?

9). Would you be more likely to use a text messaging application or Brightspace for reminders? Why?

10). How likely would you be willing to answer questions voluntarily for a text messaging application? Ask them? 

# Answers:

## Question 1:
a). No

b). No

c). Yes

## Question 2:
a). Easier for messages to show up on phone (like how Facebook pops up with a bubble).

b). Not sure, I already have an app for everything they need already.

c). Make it more convenient to communicate with people. 

## Question 3:
a). Don’t have to worry about parents (pure communication). You can call people and do stuff on your phone at the same time (Facebook call might lock you out of doing other stuff).

b). Sometimes it is just more convenient; I have started using text messaging a lot more since I got an iPhone because it is more convenient than Android text messaging.

c). You don't have to have data or wifi. 

## Question 4:
a). Do more than rely on long emails. Social media requires you to check. Appreciate the warning stuff sent as texts; it would be nice if they sent more important reminders over texts, such as add/drop period.

b). A lot of students would like to know the overall goals of the administration more clearly, such as why housing choices are made. Emails as a medium are good though, because user is willing to scroll through them rather than ignore them.

c). Vanderbilt communicates quite well already. 

## Question 5
a). Very willing to.

b). Fairly willing to do so for a friend.

c). Somewhat likely

## Question 6
a). People who are afraid to raise their hand could ask a question using text messaging.

b). Some students may be too shy to raise their hands, so if students could ask questions anonymously, students might be more willing to ask questions.

c). Use it have students to ask questions in class. 

## Question 7
a). A lot of professors prefer text over email already, but it depends on how personal the professors are. Peabody professors are a lot more personal and may be more willing to text students, as well as have group messages with students.

b). Text messages are more casual and convenient than emails, so if a student would want to ask a question, they could ask via a text message. 

c). Maybe it would be useful for facilitating a meetup or asking a question, although email is available. 

## Question 8
a). Not sure if communication needs to be improved. Platform may not be needed because there is already Piazza/find people in class. People usually don’t want to discuss things even though there are platforms.

b). If there a group text message, students might be more willing to clarify between themselves. 

c). Not sure, things seem fine now. 

## Question 9
a). Text messaging would be pretty neat. Brightspace makes it hard to juggle multiple classes because you can only open one at a time.

b). Just as likely for either one since Brightspace actually has a system to send out reminders via text message. However, the current Brightspace feature sometimes cuts off at half of the message, and you can’t see the entire message unless you log into Brightspace.

c). Text messaging.

## Question 10
a). Would be more willing to answer questions if he doesn’t get every question but only a few of them. Would be very unlikely to ask questions.

b). Not very likely to answer questions; would need some incentive to answer questions for others. Pretty unlikely to submit questions for others.

c). Very unlikely to answer questions. Fairly willing to ask questions.


# Requirements
The goal is to build on the existing framework for the text messaging application to allow students to receive reminders for classes (similar to the ones on Brightspace) via text message. Although there is currently an option to receive SMS reminders from Brightspace, half of the message is sometimes cut off and the student must log into Brightspace to view the actual reminder. 

To facilitate this, the professor would need to register himself/herself as a professor of a course, while the students would need to register themselves as students. There would be some data structure to store the different courses and the professor and students in each course. Professors would text reminders to a centralized number, and the application would automatically send reminder to students in that course. 


# Development Approach
The first step was to figure out which group of people at Vanderbilt I wanted to help. I have noticed that students around me are often stressed out about coursework, and that STEM students seems especially stressed. Moreover, as a Computer Science student, I believe that I am more able to empathize with this particular gorup.

The second step was to think of something that I personally felt would benefit students around me given my experiences at Vanderbilt. I came up with several ideas based on my experiences as a Vanderbilt STEM student. 

The first idea was for reminders to be sent out via text message instead of Brightspace, since I thought that maybe this would help alleviate some of the stress by allowing students to have easier access to what is due soon. Moreover, I have heard stories of Peabody professors who personally text their students, and the students highly appreciate the personalized nature of interactions with professors. Although STEM professors may never interact with all students on such a personalized level due to large class sizes, text messages to students may be a good first step to make the lives of students more convenient.

A second idea was to provide a platform for students to communicate anonymously with each other about classes. Right now, due to how big some STEM classes are, it may be difficult for students to know other students closely enough to ask them questions outside of class. By providing a means for students to communicate outside of class, STEM students may be able to ask questions about material they are confused about, helping them to understand the material more quickly to reduce stress.

After thinking of some ideas, my third step was to think of how these ideas would fit within the existing framework to see if it would be practical to build these applications within the allotted time period. For the reminders system, the current experts framework could be adapted to have all the students registered as "experts" (the name would be changed to something such as students instead of experts), and the topic could be replaced with the course. The professor would then text reminders to the centralized system with the course as the keyword, and the students would then be automatically forwarded the reminders. A few modifications could be added once the base framework is adapted. The professor could be given as special role as a professor so that no one else could pose as the professor and send reminders to the number. Another improvement is that the reminder could be limited to a certain number of words to prevent overly long messages, since students usually prefer more concise texts.

For the communication between students, the current experts framework could be used to have all students registered as "experts" (renamed to a different role such as students) so that all other students would receive asked questions. The topics would again be converted to the course names to have students registered in those areas. However, the current framework only allows the person who asked the question to view replies. A modification would be needed so that all students registered in the course would see the answers so that students could also benefit from the discussion.

The fourth step was to talk to STEM students and ask about what they wanted and what they used text messages for to see if 
