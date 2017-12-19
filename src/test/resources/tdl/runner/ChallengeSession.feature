Feature: Should allow the user to interact with the challenge server

  Background:
    Given There is a challenge server running on "localhost" port 8222
    And the challenge server exposes the following endpoints
      | verb       | endpointMatches        | returnStatus | returnBody                           | acceptHeader  |
      | GET        | /availableActions/(.*) | 200          | Available actions coming from server | text/coloured |
      | GET        | /roundDescription/(.*) | 200          | RoundID\nRound Description           | text/coloured |
      | GET        | /journeyProgress/(.*)  | 200          | Journey progress coming from server  | text/coloured |
      | POST       | /action/(.*)/(.*)      | 200          | Successful action feedback           | text/coloured |

    And There is a recording server running on "localhost" port 41375
    And the recording server exposes the following endpoints
      | verb       | endpointEquals    | returnStatus | returnBody   |
      | GET        | /status           | 200          | OK           |
      | POST       | /notify           | 200          | ACK          |

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
    And there is an implementation runner that prints "Running implementations"
    When user starts client
    Then the implementation runner should be run with the provided implementations
    And the server interaction should look like:
      """
      Connecting to localhost
      Journey progress coming from server
      Available actions coming from server
      Selected action is: deploy
      Running implementations
      Successful action feedback
      Challenge description saved to file: challenges/RoundID.txt.

      """
    And the recording system should be notified with "RoundID/deploy"

  # Negative paths

  Scenario: Should exit when no available actions
    Given the challenge server exposes the following endpoints
      | verb       | endpointMatches         | returnStatus | returnBody               | acceptHeader  |
      | GET        | /availableActions/(.*)  | 200          | No actions available.    | text/coloured |
    When user starts client
    Then the client should not ask the user for input

  Scenario: Should exit if recording not available
    Given recording server is returning error
    When user starts client
    Then the client should not ask the user for input
    And the user is informed that they should start the recording

#  Scenario: challenge server not available
#
#  Scenario: challenge server is returning 404


