# Catan Cooper 
*An online Settlers of Catan in the browser.*


A semester long project for the course on Software Engineering at Cooper Union (ECE-366) with Christoper Hong for the Spring 2025 semester. 

## Team

- [@surinderpalsingh](https://github.com/surinderpalsingh)
- [@IsaacMoore3514](https://github.com/IsaacMoore3514)
- [@Mohamed0510](https://github.com/Mohamed0510)


## Tech Stack
- TBD

## Demo
- TBD

## Installation 
- TBD

## Ethics 

In congruence with the requirements for the final project set forth by Professor Hong and our careers in software, our project abides by the [Software Engineering Code of Ethics and Professional Practice](https://www.acm.org/code-of-ethics/software-engineering-code) in the following ways:

1.  *2.01. Provide service in their areas of competence, being honest and forthright about any limitations of their experience and education.*. 

The implementation of the longest-road algorithm with the restriction that there are no cycles is an NP-hard problem that requires a depth-first search and is an algorithm that all group members were forthcoming in their lack of experience in implementing. As a result, the requirements of the project were eventually relaxed to exclude the longest-road objective in Catan and instead count the number of roads placed by a player with our supervising authority within the semester's scheduling constraints. 

2. *2.06. Identify, document, collect evidence and report to the client or the employer promptly if, in their opinion, a project is likely to fail, to prove too expensive, to violate intellectual property law, or otherwise to be problematic.*

The implementation of real-time game updates across all clients in the same game lobby is a technical problem that posed severe problems to the technical feasibility of the project for students new to Java and juggling other classes. However, the addition of regular weekly meetings to catch-up in-person in the 6th floor Microlab was extremely helpful in tracking progress across the weeks, without which we would have likely failed in creating a final playable game. 

3. *3.02. Ensure proper and achievable goals and objectives for any project on which they work or propose.*

Breaking the project down into three sizeable chunks: the database, the app service layer, and the front-end allowed for smaller more achievable goals to be realized and for objectives on the project to be rigorously met. As a result, we were able to hit our sprint goals as deadlines came up every two weeks without piling on unfinished features in a backlog waiting to be implemented within the main branch. 

4. *3.04. Ensure that they are qualified for any project on which they work or propose to work by an appropriate combination of education and training, and experience.*

Regular attendance in the software-engineering class exposed the group to Docker networking, Maven product lifecycle, React, authentication flows, and JaCoCo testing. Without this peristent education throughout the semester and consistent attendance, the lecture slides, in addition to working remote repos hosted on Github, proved as an invaluable base for helping to work through bugs. 

5. *3.05. Ensure an appropriate method is used for any project on which they work or propose to work.*

We chose industry-proven tools, like React and Typescript for the front-end, Java+Spring Boot for the API and Git for version control. These are tools that are ubiquitous in the industry and are battle-proven with a lot of documentation and resources available on the internet behind them. Using Git fostered team collaboration letting us work on features concurrently without stepping on eachother's toes. 

6. *3.08. Ensure that specifications for software on which they work have been well documented, satisfy the users’ requirements and have the appropriate approvals.*

REST API endpoints were first sketched out after whiteboarding out our database early in the semester which clarified which requirements the users were going to need and what operations would need to occur on the database to have a funcitoning app. 

7. *3.10. Ensure adequate testing, debugging, and review of software and related documents on which they work.*

Unit tests for business logic and our Spring Boot services through Mockito were present and throughout the semester teammates read the related documentation to understand the ins-and-outs of the technology before implementing the tech stack. 

8. *8.02. Improve their ability to create safe, reliable, and useful quality software at reasonable cost and within a reasonable time.*

The app is spun up on Azure for free using student credits at an extremely reasonable cost which means zero hosting bills for the duration of hosting towards the end of the semester. Because our GitHub repo is public and the source code is available publicly and can be cloned on local machines and the requirements to run the code are easy to procure anyone can play CooperCatan. A youtube demo is also linked for those who are less technically-inclined and still want to see the app and its functions without having to clone the repo locally on their machine.  

9. *8.03. Improve their ability to produce accurate, informative, and well-written documentation.*

Documentation was well-written with features for the backend/database and the frontend being implemented on different branches which were merged in main to keep the entire project clean. Also, documentation was important with the group learning best documentation practices, for example, listing requirements/dependencies, how to install, project structure, file structure, etc. 

10. *8.04. Improve their understanding of the software and related documents on which they work and of the environment in which they will be used.*

To understand the wide variety of the technologies being used, attendance in office-hours were critical to understand how the different pieces fit together and networked. Now, all apps are understood as databases, service layers, and frontends which display those changes to the user. 





