# Created by julianghionoiu at 11/10/2015
Feature: #Enter feature name here
  # Enter feature description here

  Background:
    Given I start with a clean broker

  Scenario: Successfully process messages
    Given I receive the following requests:
      | X1, 0, 1  |
      | X2, 5, 6  |
    When I go live with an implementation that adds to numbers
    Then the client should consume all requests
    And the client should publish the following responses:
      | X1, 1    |
      | X2, 11   |


  Scenario: Display requests and response
    Given I receive the following requests:
      | X1, 0, 1  |
    When I go live with an implementation that adds to numbers
    Then the client should display to console:
      | id = X1, req = [0, 1], resp = 1  |