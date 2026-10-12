package com.paytaxihub;

import com.paytaxihub.controller.DemoFeatureController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoFeatureControllerTest {
 @Test void notificationCanBeQueuedAndRetrievedForItsUser() {
  DemoFeatureController.notifyUser(987654,"Payment received","R42.00 credited");
  assertTrue(DemoFeatureController.hasNotification(987654));
  assertFalse(DemoFeatureController.hasNotification(987655));
 }
 @Test void ownerMfaChallengeIsUniqueAndStored() {
  String a=DemoFeatureController.issueOwnerMfa(1), b=DemoFeatureController.issueOwnerMfa(1);
  assertNotNull(a); assertNotEquals(a,b);
 }
}
