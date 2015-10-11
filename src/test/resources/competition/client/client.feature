# Created by julianghionoiu at 11/10/2015
Feature: Complete challenge
  # Enter feature description here

  Background:
    Given I start with a clean broker

  Scenario: Successfully process messages
    Given I receive the following requests:
      | X1, 0, 1  |
      | X2, 5, 6  |
    When I go live with an implementation that adds two numbers
    Then the client should consume all requests
    And the client should publish the following responses:
      | X1, 1    |
      | X2, 11   |


  Scenario: Display requests and response
    Given I receive the following requests:
      | X1, 0, 1  |
    When I go live with an implementation that adds two numbers
    Then the client should display to console:
      | id = X1, req = [0, 1], resp = 1  |


  Scenario: Handle null responses
    Given I receive the following requests:
      | X1, 0, 1  |
    When I go live with an implementation that returns null
    Then the client should not consume any request
    And the client should not publish any response


  Scenario: Handle exceptions
    Given I receive the following requests:
      | X1, 0, 1  |
    When I go live with an implementation that throws exception
    Then the client should not consume any request
    And the client should not publish any response


  #  Connections problems
  #DEBT: Should be handled as a separate feature

  Scenario: Exit gracefully is broker not available
    Given the broker is not available
    When I go live with an implementation that is valid
    Then I should get no exception


  #  Trial runs
  #DEBT: Should be handled as a separate feature

  Scenario: Trial run does not count
    Given I receive the following requests:
      | X1, 0, 1  |
      | X2, 5, 6  |
    When I do a trial run with an implementation that adds two numbers
    Then the client should not consume any request
    And the client should not publish any response

  Scenario: Trial run displays first message
    Given I receive the following requests:
      | X1, 0, 1  |
      | X2, 5, 6  |
    When I do a trial run with an implementation that adds two numbers
    Then the client should display to console:
      | id = X1, req = [0, 1], resp = 1  |
    But the client should not display to console:
      | id = X2, req = [5, 6], resp = 11  |