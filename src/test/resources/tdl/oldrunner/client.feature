Feature: Should query and read information from server

  Background:
    Given I start with a clean server

  Scenario: Should show journey progress and available actions when querying for information
    # Might want to move the server setup to the Background task
    Given server is running with basic setup
    When I check the status of a challenge
    Then the client should query the following endpoints:
      | endpoint         |
      | journeyProgress  |
      | availableActions |

  Scenario: Should show no available actions
    Given server has no available actions
    When I check the status of a challenge
    Then the client should find there are no available actions