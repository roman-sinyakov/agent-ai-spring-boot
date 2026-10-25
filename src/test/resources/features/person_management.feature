Feature: Person management
  Clients keep a register of people: they list, read, create, update and delete persons.

  Background:
    Given the client has read and write access

  @01-AC-1 @API-3 @API-9
  Scenario: Creating a person
    When the client creates a person with:
      | firstName   | Jane                 |
      | lastName    | Doe                  |
      | email       | Jane.Doe@Example.com |
      | dateOfBirth | 1990-05-20           |
      | phoneNumber | +48123456789         |
    Then the person is created
    And the person has the e-mail "jane.doe@example.com"

  @01-AC-2 @API-1 @API-6
  Scenario: Persons are listed by last name, then first name
    Given a person "John Smith" with e-mail "john.smith@example.com" exists
    And a person "Jane Doe" with e-mail "jane.doe@example.com" exists
    And a person "Adam Doe" with e-mail "adam.doe@example.com" exists
    When the client lists persons
    Then the request succeeds
    And the persons are listed in this order:
      | Adam Doe   |
      | Jane Doe   |
      | John Smith |

  @01-AC-3 @API-2
  Scenario: Reading a person
    Given a person "Jane Doe" with e-mail "jane.doe@example.com" exists
    When the client reads the person "Jane Doe"
    Then the request succeeds
    And the person is returned as it was created

  @01-AC-4 @API-2 @API-12 @API-13
  Scenario: Reading a person that does not exist
    When the client reads the person with id "999"
    Then the person is not found with the message "Person with id '999' was not found"

  @01-AC-5 @API-10 @API-13
  Scenario: Invalid data is rejected
    When the client creates a person with:
      | firstName   |              |
      | lastName    | Doe          |
      | email       | not-an-email |
      | dateOfBirth | 2090-05-20   |
      | phoneNumber | abc          |
    Then the request is rejected as invalid
    And these fields are reported as invalid:
      | firstName   |
      | email       |
      | dateOfBirth |
      | phoneNumber |

  @01-AC-6 @API-11
  Scenario: E-mail must be unique regardless of letter case
    Given a person "Jane Doe" with e-mail "jane.doe@example.com" exists
    When the client creates a person "Joanna Doe" with e-mail "Jane.Doe@example.com"
    Then the request is rejected as a conflict

  @01-AC-7 @API-4 @API-8
  Scenario: Updating a person keeps the creation time
    Given a person "Jane Doe" with e-mail "jane.doe@example.com" exists
    When the client changes the last name of "Jane Doe" to "Smith"
    Then the request succeeds
    And the person has the last name "Smith"
    And the person keeps its creation time and has a later modification time

  @01-AC-8 @API-5
  Scenario: Deleting a person
    Given a person "Jane Doe" with e-mail "jane.doe@example.com" exists
    When the client deletes the person "Jane Doe"
    Then the request succeeds without content
    And the person "Jane Doe" no longer exists
