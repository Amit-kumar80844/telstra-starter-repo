Feature: SIM Card Activation

  As a system
  I want to activate SIM cards via POST requests
  So that I can validate whether activation is successful or failed

  Scenario: Successful SIM card activation
    Given the SIM card ICCID is "1255789453849037777" and the customer email is "user@example.com"
    When I send an activation request
    Then the activation should be successful
    And the activation record with ID 1 should exist

  Scenario: Failed SIM card activation
    Given the SIM card ICCID is "8944500102198304826" and the customer email is "user@example.com"
    When I send an activation request
    Then the activation should fail
    And the activation record with ID 2 should not exist
