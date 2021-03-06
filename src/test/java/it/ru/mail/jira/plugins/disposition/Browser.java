package it.ru.mail.jira.plugins.disposition;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Browser extends FirefoxDriver implements WebDriver {
    private String homeUrl;
    private final int QUEUE_START = 1;
    private final int QUEUE_END = 10;
    private List<String> queue = new ArrayList<String>();

    public Browser(){
        super();
        homeUrl = JiraUrls.localHome;
        get(homeUrl);
        manage().window().maximize();
    }

    public Browser(String url){
        super();
        homeUrl = url;
        get(homeUrl);
        manage().window().maximize();
    }

    public void loginWithUsernameAndPassword(String username, String password){
        get(homeUrl);
        switchTo().frame(findElementByXPath(ElementsXPath.loginFrame));
        findElementByXPath(ElementsXPath.loginField).sendKeys(username);
        findElementByXPath(ElementsXPath.passwordField).sendKeys(password);
        findElementByXPath(ElementsXPath.loginButton).click();
        sleep(10000);
    }

    public void logOut(){
        get(homeUrl);
        findElementByXPath(ElementsXPath.userMenu).click();
        findElementByXPath(ElementsXPath.logoutButton).click();
        get(homeUrl);
    }

    public void openIssuesList(){
        get(url(JiraUrls.issues));
        findElementByXPath(ElementsXPath.searchButton).click();
        sleep(3000);
    }

    public void sortIssues(){
        findElementByXPath(ElementsXPath.sortByQueueButton).click();
        sleep(2000);
        int upperRowNumber = Integer.parseInt(findElementByXPath("//tr[3]/td[11]").getText());
        int lowerRowNumber = Integer.parseInt(findElementByXPath("//tr[4]/td[11]").getText());
        if(upperRowNumber>lowerRowNumber){
            findElementByXPath(ElementsXPath.sortByQueueButton).click();
        }
        sleep(2000);
    }

    public void moveIssue(int fromPos, int toPos){
        resetLocalQueue();
        moveIssueLocally(fromPos, toPos);
        moveIssueInQueue(fromPos, toPos);
        sleep(5000);
    }

    public boolean queueCorrect(){
        String key;
        String localkey;
        for(int i=QUEUE_START; i<=QUEUE_END; i++)
        {
            key = getTicketKeyByQueuePosition(i);
            localkey = queue.get(i-1);
            System.out.println(key+"   "+localkey);
            if(!key.equals(localkey)){
                return false;
            }
        }
        return true;
    }

    public void moveIssueInQueue(int fromPos, int toPos){
        int yOffset = fromPos < toPos ? 5 : -5;
        WebElement fromRow = getRowByQueuePosition(fromPos);
        WebElement toRow = getRowByQueuePosition(toPos);
        Actions builder = new Actions(this);
        builder.clickAndHold(fromRow)
                .moveToElement(toRow)
                .moveByOffset(5, yOffset)
                .release()
                .build()
                .perform();
    }

    public void createIssues(int count) {
        goHome();
        waitForElement(ElementsXPath.issuesMenu, 5).click();
        findElementByXPath(ElementsXPath.createIssue).click();
        waitForElement(ElementsXPath.createAnotherCheckbox, 5).click();
        for(int i=1; i<count; i++) {
            findElementByXPath(ElementsXPath.createIssueSummaryField).sendKeys("i" + i);
            findElementByXPath(ElementsXPath.createIssueSubmitButton).click();
            sleep(2000);
        }
        findElementByXPath(ElementsXPath.createAnotherCheckbox).click();
        findElementByXPath(ElementsXPath.createIssueSummaryField).sendKeys("i" + count);
        findElementByXPath(ElementsXPath.createIssueSubmitButton).click();
        sleep(2000);
    }

    public void reindexIssues(){
        get(url(JiraUrls.reindexIssues));
        findElementByXPath(ElementsXPath.reindexButton).click();
    }

    public boolean issuesEnumerated(){
        try{
            for(int i=QUEUE_START; i<=QUEUE_END; i++) {
                getRowByQueuePosition(i);
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    public void issueIsClosed() {
        openIssuesList();
        findElementByXPath(String.format(ElementsXPath.issueToolButton, 1)).click();
        findElementByXPath(ElementsXPath.closeOption).click();
        findElementByXPath(ElementsXPath.closeIssueButton).click();
    }

    public boolean noClosedTicketsInQueue()
    {
        try{
            getRowByQueuePosition(10);
        }
        catch (Exception e){
            return true;
        }
        return false;

    }

    public void removeIssues() {
        openIssuesList();
        findElementByXPath(ElementsXPath.issueTableToolButton).click();
        waitForElement(ElementsXPath.editAllOption, 2).click();
        findElementByXPath(ElementsXPath.allIssuesCheckbox).click();
        findElementByXPath(ElementsXPath.nextButton).click();
        findElementByXPath(ElementsXPath.deleteOption).click();
        findElementByXPath(ElementsXPath.nextButton).click();
        findElementByXPath(ElementsXPath.confirmButton).click();
    }

    public void createProject(String projectName) {
        get(url(JiraUrls.createProject));
        findElementByXPath(ElementsXPath.projectNameField).sendKeys(projectName);
        findElementByXPath(ElementsXPath.addProjectButton).click();
    }

    public void preconfigure(){
        configureGroups();
        createQueueField();
        //в таблице должен быть хотя бы один тикет
        //чтобы можно было настроить поля для отображения
        createIssues(1);
        configureIssuesTable();
        removeIssues();
    }

    private void configureGroups() {
        createGroups();
        addMember("admin");
    }

    private void createQueueField() {
        get(url(JiraUrls.customFields));
        findElementByXPath(ElementsXPath.addCustomFieldButton).click();
        sleep(3000);
        findElementByXPath(ElementsXPath.issueDispositionOption).click();
        findElementByXPath(ElementsXPath.addCFNextButton).click();
        findElementByXPath(ElementsXPath.fieldNameInput).sendKeys("Disposition");
        findElementByXPath(ElementsXPath.addCFNextButton).click();
        List<WebElement> checkboxes = findElementsByXPath(ElementsXPath.screenCheckboxes);
        for(int i=0; i<3; i++){
            checkboxes.get(i).click();
        }
        findElementByXPath(ElementsXPath.submitButton).click();
        findElementByXPath(ElementsXPath.customFieldToolsButton).click();
        waitForElement(ElementsXPath.configureFieldOption, 5).click();
        findElementByXPath(ElementsXPath.editJqlLink).click();
        findElementByXPath(ElementsXPath.jqlField).sendKeys("Status in (Open, \"In Progress\", Reopened) order by \"Disposition\" asc, \"Key\" desc");
        findElementByXPath(ElementsXPath.submitButton).click();
    }

    private void configureIssuesTable() {
        get(url(JiraUrls.editIssueViewColumns));
        findElementByXPath(String.format(ElementsXPath.hideColumnButton, "Summary")).click();
        findElementByXPath(ElementsXPath.selectColumnDD).sendKeys("Di");
        findElementByXPath(ElementsXPath.addColumnButton).click();
    }

    private void createGroups(){
        get(url(JiraUrls.groups));
        findElementByXPath(ElementsXPath.groupNameField).sendKeys("Disposition");
        findElementByXPath(ElementsXPath.addGroupButton).click();
        findElementByXPath(ElementsXPath.groupNameField).sendKeys("queueManager");
        findElementByXPath(ElementsXPath.addGroupButton).click();
    }

    private void addMember(String member){
        get(url(JiraUrls.editMembers));
        Actions builder = new Actions(this);
        WebElement disposition = findElementByXPath(String.format(ElementsXPath.groupOption, "Disposition"));
        WebElement queueManager = findElementByXPath(String.format(ElementsXPath.groupOption, "queueManager"));
        builder.keyDown(Keys.CONTROL)
                .click(disposition)
                .click(queueManager)
                .keyUp(Keys.CONTROL)
                .build()
                .perform();
        findElementByXPath(ElementsXPath.addUsersToGroupInput).sendKeys(member);
        findElementByXPath(ElementsXPath.joinButton).click();
    }

    private String url(String path){
        return homeUrl + path;
    }

    private void goHome() {
        get(url(JiraUrls.dashboard));
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getTicketKeyByQueuePosition(int pos){
        String xpath = String.format(ElementsXPath.queueKeyOnPos, pos);
        return findElementByXPath(xpath).getText();
    }

    private WebElement waitForElement(String xpath, int timeoutSeconds){
        manage().timeouts().implicitlyWait(timeoutSeconds, TimeUnit.SECONDS);
        return findElementByXPath(xpath);
    }

    private WebElement getRowByQueuePosition(int pos){
        String xpath = String.format(ElementsXPath.rowOnPos, pos);
        return waitForElement(xpath, 5);
    }

    private void resetLocalQueue(){
        queue.clear();
        createLocalQueue();
    }

    private void createLocalQueue(){
        String ticketId;
        for(int i=QUEUE_START; i<=QUEUE_END; i++)
        {
            ticketId = getTicketKeyByQueuePosition(i);
            queue.add(i-1, ticketId);
        }
    }

    private void moveIssueLocally(int fromPos, int toPos){
        String movedTicketId = queue.get(fromPos-1);
        if(fromPos<toPos){
            for(int i=fromPos-1; i<toPos-1; i++)
            {
                queue.set(i, queue.get(i + 1));
            }
        }
        else
        {
            for(int i=fromPos-1; i>toPos-1; i--)
            {
                queue.set(i, queue.get(i - 1));
            }
        }
        queue.set(toPos-1, movedTicketId);
    }


}
