Feature: Should query and read information from server

  Background:
    Given There is a challenge server running on "localhost" port 8222
    And It exposes the following endpoints
      | verb       | endpoint             | returnStatus | returnBody                             |
      | GET        | journeyProgress      | 200          | "Journey progress coming from server"  |
      | GET        | availableActions     | 200          | "Available actions coming from server" |
      | GET        | roundDescription     | 200          | "RoundID\nRound Description"           |
      | POST       | action/*             | 200          | "Successful action feedback"           |
    And expects requests to have the Accept header set to "text/coloured"

  # Business critical scenarios

  Scenario: The server interaction
    When user starts client
    Then the user should see:
      """
      Journey progress coming from server
      Available actions coming from server
      """
    Then the client should ask the user for input and wait
    When user types action "anySuccessful"
    Then the user should see:
      """
      Successful action feedback
      """
    And the client should exit

  Scenario: Refresh round description on successful action
    When user starts client
    And types action "anySuccessful"
    Then the file "challenges/RoundID.txt" should contain "Round Description"
    And the recording system should be notified with "RoundID/new"

  Scenario: Deploy code to production and display feedback
    When user starts client
    And types action "deploy"
    Then the queue client should be run with the provided implementations
    And the user should see:
      """
      Successful action feedback
      """
    And the recording system should be notified with "RoundID/deploy"

  # Negative paths

  Scenario: Should exit when no available actions
    Given server endpoint "availableActions" returns "No available actions"
    When user starts client
    Then the client should not ask the user for input

  Scenario: Should exit if recording not available
