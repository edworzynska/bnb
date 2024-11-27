# BnB Booking Platform API

The __BnB Booking Platform API__ allows users to list living spaces and offer them to other users of the platform. This __RESTful API__ supports functionality to create, view, book, and manage users' spaces with an integrated email notification service. 

## Tech Stack

- **Java**: Core programming language.
- **Spring Boot**: Framework for building the application  
- **Spring Security**: Provides authentication and authorization  
- **Hibernate**: ORM for database management  
- **PostgreSQL**: Primary database  
- **H2 Database**: In-memory database for testing  
- **Lombok**: Reduces boilerplate code  
- **JUnit**: Testing framework  
- **Mockito**: Mocking framework  

## The API allows:

- User registration with password and email validation
- Listing and booking spaces, with features to add available dates, view and filter spaces by date
- Booking management including approving or denying booking requests
- Email Notifications informing users of booking updates and status changes

## Key Learnings

During the development of this project, I learned how to effectively plan, design, test-drive, and engineer a **RESTful API**. My focus was primarily on planning the project, as the entire application was meant to be completed within 5 days. This required careful consideration of the essential features for an **MVP** (Minimum Viable Product). I observed the importance of keeping the code clean and well-tested, especially as the number of lines of code grew. Creating separate services for bookings, users, and spaces made the code easier to understand, maintain, and test.

### Installation Guide

#### Prerequisites
- Java JDK 17 or higher
- Gradle
- PostgreSQL (H2 is only used for testing)
  
#### Steps:

- Clone the Repository:
git clone https://github.com/edworzynska/bnb
- Set Environment Variables:
For the email notification service to work in production, set up an environment variable with the email password. During testing, this is not required, as the service is mocked.
- Create PostgreSQL Database:
Set up a new PostgreSQL database to use with this application.
- Build and Run the Application:
Navigate to the project directory and use Gradle to build and run the app:
./gradlew build
./gradlew run
The application will start on the default port: http://localhost:8080
