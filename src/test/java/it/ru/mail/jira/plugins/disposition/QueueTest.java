package it.ru.mail.jira.plugins.disposition;

import org.junit.*;

import static org.junit.Assert.assertTrue;

public class QueueTest {
    private static final String CI_URL="http://192.168.0.210:2990/jira/secure/Dashboard.jspa";
    private static final String LOCAL_URL="http://localhost:2990/jira/secure/Dashboard.jspa";

    private static Browser browser = new Browser(LOCAL_URL);

    @BeforeClass
    public static void LogIn(){
        browser.manage().window().maximize();
        browser.loginWithUsernameAndPassword("admin", "admin");
    }

    @AfterClass
    public static void LogOut(){
        browser.logOut();
        browser.quit();
    }

    @Test
    public void testMoveIssueUp() throws InterruptedException {
        moveIssue(10, 1);
    }

    @Test
     public void testMoveIssueDown() throws InterruptedException {
        moveIssue(2, 7);
    }

    private void moveIssue(int fromPos, int toPos) throws InterruptedException {
        browser.openIssuesList();
        browser.resetLocalQueue();
        browser.moveIssue(fromPos, toPos);
        //Wait for page to refresh
        Thread.sleep(1000);
        assertTrue(browser.queueCorrect());
    }
}