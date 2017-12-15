Feature: Should query and read information from server

  Background:
    Given There is a challenge server running on "localhost" port 8222
    And the challenge server exposes the following endpoints
      | verb       | endpoint             | returnStatus | returnBody                           |
      | GET        | journeyProgress      | 200          | Journey progress coming from server  |
      | GET        | availableActions     | 200          | Available actions coming from server |
      | GET        | roundDescription     | 200          | RoundID\nRound Description           |
      | POST       | action/([a-zA-Z]+)   | 200          | Successful action feedback           |
    And expects requests to have the Accept header set to "text/coloured"

    And There is a recording server running on "localhost" port 41375
    And the recording server exposes the following endpoints
      | verb       | endpoint          | returnStatus | returnBody   |
      | GET        | status            | 200          | OK           |
    And the challenges folder is empty

  # Business critical scenarios

  Scenario: The server interaction
    When user starts client with action "anySuccessful"
    Then the user should see:
      """
      Journey progress coming from server
      Available actions coming from server
      """
    And the user should see:
      """
      Successful action feedback
      """

  Scenario: Refresh round description on successful action
    When user starts client with action "anySuccessful"
    Then the recording system should be notified with "RoundID/new"
    And the client should exit
    And the file "challenges/RoundID.txt" should contain
    """
    RoundID
    Round Description

    """

  Scenario: Deploy code to production and display feedback
    When user starts client
    And types action "deploy"
    Then the queue client should be run with the provided implementations
    And the user should see:
      """
      Successful action feedback
      """
    And the recording system should be notified with "RoundID/deploy"

#
#  Scenario: Should exit if recording not available
