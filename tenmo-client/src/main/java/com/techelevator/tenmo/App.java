package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AccountMgmtService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.util.BasicLogger;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

public class App {
    
    private static final String API_BASE_URL = "http://localhost:8080/";
    
    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private AuthenticatedUser currentUser;
    private AccountMgmtService accountMgmtService;
    
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }
    
    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null)
        {
            mainMenu();
        }
    }
    
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null)
        {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1)
            {
                handleRegister();
            }
            else if (menuSelection == 2)
            {
                handleLogin();
            }
            else if (menuSelection != 0)
            {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }
    
    private void handleRegister()
    {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials))
        {
            System.out.println("Registration successful. You can now login.");
        }
        else
        {
            consoleService.printErrorMessage();
        }
    }
    
    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
        accountMgmtService = new AccountMgmtService(API_BASE_URL, currentUser);
    }
    
    private void mainMenu()
    {
        int menuSelection = -1;
        while (menuSelection != 0)
        {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1)
            {
                viewCurrentBalance();
            }
            else if (menuSelection == 2)
            {
                viewTransferHistory();
            }
            else if (menuSelection == 3)
            {
                viewPendingRequests();
            }
            else if (menuSelection == 4)
            {
                sendBucks();
            }
            else if (menuSelection == 5)
            {
                requestBucks();
            }
            else if (menuSelection == 0)
            {
                continue;
            }
            else
            {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }
    
    private void viewCurrentBalance()
    {
        consoleService.printCurrentBalance(accountMgmtService.viewCurrentBalance());
    }
    
    private void viewTransferHistory()
    {
        Transfer[] transfers = accountMgmtService.viewTransferHistory();
        consoleService.printTransferHistory(transfers, currentUser.getUser().getId());
        consoleService.printTransfer(consoleService.promptForMenuSelection("Please enter transfer ID to view details (0 to cancel): "), transfers, currentUser.getUser().getId());
        mainMenu();
    }
    
    private void viewPendingRequests()
    {
        Transfer[] transferArray = accountMgmtService.pendingRequest(currentUser.getUser().getId());
        Transfer chosenTransfer = null;
        consoleService.pendingRequestMenu(transferArray);
        long transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");
        for (int i = 0; i < transferArray.length; i++)
        {
            if (transferArray[i].getTransferId().equals(transferId))
            {
                chosenTransfer = transferArray[i];
            }
        }
        if (chosenTransfer == null)
        {
            System.out.println("I'm sorry, that's not a valid transfer.");
            mainMenu();
        }
        consoleService.printApproveOrRejectRequest();
        long decision = consoleService.promptForInt("Please choose an option: ");
        accountMgmtService.approveOrReject(decision, chosenTransfer);

    }


    // decided to return a boolean because it's a PUT method.
    private void sendBucks() {
        consoleService.printSendMoneyMenu(accountMgmtService.getUserList(), currentUser.getUser().getId());
        long receiverId = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel): ");
        BigDecimal amountToSend = consoleService.promptForBigDecimal("Enter amount: ");
        accountMgmtService.sendBucks(currentUser.getUser().getId(), receiverId, amountToSend);
        mainMenu();
    }
    
    private void requestBucks() {
        consoleService.printRequestMoneyMenu(accountMgmtService.getUserList(), currentUser.getUser().getId());
        long receiverId = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");
        BigDecimal amountToReceive = consoleService.promptForBigDecimal("Enter amount: ");
        accountMgmtService.requestBucks(currentUser.getUser().getId(), receiverId, amountToReceive);
        mainMenu();
    }
    
}














