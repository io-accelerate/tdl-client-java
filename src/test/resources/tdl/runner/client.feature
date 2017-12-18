Feature: Should allow the user to interact with the challenge server

  Background:
    Given There is a challenge server running on "localhost" port 8222
    And the challenge server exposes the following endpoints
      | verb       | endpoint             | returnStatus | returnBody                           |
      | GET        | journeyProgress      | 200          | Journey progress coming from server  |
      | GET        | availableActions     | 200          | Available actions coming from server |
      | GET        | roundDescription     | 200          | RoundID\nRound Description           |
      | POST       | action/*             | 200          | Successful action feedback           |
    And the challenge server expects requests to have the Accept header set to "text/coloured"

    And There is a recording server running on "localhost" port 41375
    And the recording server exposes the following endpoints
      | verb       | endpoint          | returnStatus | returnBody   |
      | GET        | status            | 200          | OK           |
      | POST       | notify            | 200          | ACK          |

  # Business critical scenarios

  Scenario: The server interaction
    Given the action input comes from a provider returning "anySuccessful"
    And the challenges folder is empty
    When user starts client
    Then the server interaction should look like:
      """
      Connecting to localhost
      Journey progress coming from server
      Available actions coming from server
      Selected action is: anySuccessful
      Successful action feedback
      Challenge description saved to file: challenges/RoundID.txt.

      """

  Scenario: Refresh round description on successful action
    Given the action input comes from a provider returning "anySuccessful"
    And the challenges folder is empty
    When user starts client
    Then the file "challenges/RoundID.txt" should contain
    """
    RoundID
    Round Description

    """
    And the recording system should be notified with "RoundID/new"

  Scenario: Deploy code to production and display feedback
    Given the action input comes from a provider returning "deploy"
    When user starts client
    Then the implementation runner should be run with the provided implementations
    And the server interaction should look like:
      """
      Successful action feedback
      """
    And the recording system should be notified with "RoundID/deploy"

  # Negative paths

  Scenario: Should exit when no available actions
    Given server endpoint "availableActions" returns "No actions available."
    When user starts client
    Then the client should not ask the user for input
#
#  Scenario: Should exit if recording not available
