Feature: Should query and read information from server

  Background:
    Given I start with a clean server

  Scenario: Should show journey progress and available actions when querying for information
    # Might want to move the server setup to the Background task
    Given server is running with basic setup
    When user checks the status of a challenge
    Then the client should query the following endpoints:
      | endpoint         | methodType |
      | journeyProgress  | GET        |
      | availableActions | GET        |

  Scenario: Should show no available actions
    Given server has no available actions
    When user checks the status of a challenge
    Then the client should find there are no available actions

#  // what you want to know is:
#  // whether the deploy callback is called when deploy is AND isn't called.
#  // that the client goes live with the processing rules?
#  // that getRoundDescription is called when an action is called
#  // that the correct action is called

  Scenario: Challenge should start on request
    Given server is running with basic setup
    When user enters input "start"
    Then the client should query the following endpoints:
      | endpoint         | methodType |
      | action/start     | POST       |
      | roundDescription | GET        |