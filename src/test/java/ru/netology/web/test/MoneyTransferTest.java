package ru.netology.web.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.LoginPage;
//import kotlin.collections.*;


import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.generateInvalidAmount;
import static ru.netology.web.data.DataHelper.generateValidAmount;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    DataHelper.CardInfo fistCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup(){
        var loginPage = open("http://localhost:9999",LoginPage.class);
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCode(authInfo);
        dashboardPage = verificationPage.validVerify(verificationCode);
        fistCardInfo = DataHelper.getFirstCardInfo();
        secondCardInfo = DataHelper.getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(fistCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void shouldTransferMoneyFromFirstToSecond() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount),fistCardInfo);
        dashboardPage.reloadDashboardPage();
        assertAll(

                () -> dashboardPage.checkCardBalance(fistCardInfo, expectedBalanceFirstCard),
                () -> dashboardPage.checkCardBalance(secondCardInfo, expectedBalanceSecondCard)
        );
    }
    @Test
    void shouldGetErrorMessageIFAmountMoreBalance() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(fistCardInfo);
       transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);

       assertAll(() -> transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающий остаток на карте списания"),
                () -> dashboardPage.reloadDashboardPage(),
                () -> dashboardPage.checkCardBalance(fistCardInfo, firstCardBalance),
                () -> dashboardPage.checkCardBalance(secondCardInfo, secondCardBalance)
        );
    }

}
