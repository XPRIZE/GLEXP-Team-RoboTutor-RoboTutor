package cmu.xprize.comp_bigmath;

import static cmu.xprize.comp_bigmath.BM_CONST.ONE_DIGIT;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;

/**
 * BigMathProblemState
 *
 * holds state variables.
 * <p>
 * Created by kevindeland on 10/2/18.
 */

public class BigMathProblemState {


    // ------------------
    // BEGIN NEEDED FIELDS
    // ------------------

    // outer loop level
    private CBigMath_Data data;

    // inner loop level
    private boolean canTapOnes;
    private boolean canTapTens;
    private boolean canTapHuns;

    // --------------------------------------
    // ---- CURRENT CONCRETE UNIT VALUES ----
    // --------------------------------------
    public String currentDigit;

    private int currentOpAHun;
    private int currentOpBHun;
    private int resultHun;

    private int currentOpATen;
    private int currentOpBTen;
    private int resultTen;

    private int currentOpAOne;
    private int currentOpBOne;
    private int resultOne;

    private int subtrahendHun;
    private int subtrahendTen;
    private int subtrahendOne;

    private int minuendHun;
    private int minuendTen;
    private int minuendOne;


    private boolean isCarrying;
    private boolean isBorrowing;

    private boolean hasBorrowedHun;
    private boolean hasBorrowedTen;

    public BigMathProblemState(CBigMath_Data data) {
        this.data = data;

        resetState();
        resetResultUnitsAddition();
        resetResultUnitsSubtraction();
        initializeUnitValues();
    }


    /**
     * Reset some variables used to control logic.
     */
    private void resetState() {
        isBorrowing = false;
        isCarrying = false;
        hasBorrowedHun = false;
        hasBorrowedTen = false;

        currentDigit = ONE_DIGIT;
    }

    /**
     * Reset result values to 0
     */
    private void resetResultUnitsAddition() {
        resultOne = 0;
        resultTen = 0;
        resultHun = 0;
    }

    /**
     * Reset subtract values.
     */
    private void resetResultUnitsSubtraction() {
        subtrahendOne = 0;
        subtrahendTen = 0;
        subtrahendHun = 0;

        minuendOne = getOnesDigit(data.dataset[0]);
        minuendTen = getTensDigit(data.dataset[0]);
        minuendHun = getHunsDigit(data.dataset[0]);
    }

    /**
     * Initialize field values needed for BaseTen
     */
    private void initializeUnitValues() {
        currentOpAHun = getHunsDigit(data.dataset[0]);
        currentOpATen = getTensDigit(data.dataset[0]);
        currentOpAOne = getOnesDigit(data.dataset[0]);

        currentOpBHun = getHunsDigit(data.dataset[1]);
        currentOpBTen = getTensDigit(data.dataset[1]);
        currentOpBOne = getOnesDigit(data.dataset[1]);
    }


    public CBigMath_Data getData() {
        return data;
    }

    public void setData(CBigMath_Data data) {
        this.data = data;
    }

    public boolean isCanTapOnes() {
        return canTapOnes;
    }

    public void setCanTapOnes(boolean canTapOnes) {
        this.canTapOnes = canTapOnes;
    }

    public boolean isCanTapTens() {
        return canTapTens;
    }

    public void setCanTapTens(boolean canTapTens) {
        this.canTapTens = canTapTens;
    }

    public boolean isCanTapHuns() {
        return canTapHuns;
    }

    public void setCanTapHuns(boolean canTapHuns) {
        this.canTapHuns = canTapHuns;
    }

    public String getCurrentDigit() {
        return currentDigit;
    }

    public void setCurrentDigit(String currentDigit) {
        this.currentDigit = currentDigit;
    }

    public int getCurrentOpAHun() {
        return currentOpAHun;
    }

    public void setCurrentOpAHun(int currentOpAHun) {
        this.currentOpAHun = currentOpAHun;
    }

    public void decrementCurrentOpAHun() {
        this.currentOpAHun--;
    }

    public int getCurrentOpBHun() {
        return currentOpBHun;
    }

    public void setCurrentOpBHun(int currentOpBHun) {
        this.currentOpBHun = currentOpBHun;
    }

    public void decrementCurrentOpBHun() {
        this.currentOpBHun--;
    }

    public int getResultHun() {
        return resultHun;
    }

    public void setResultHun(int resultHun) {
        this.resultHun = resultHun;
    }

    public void incrementResultHun() {
        this.resultHun++;
    }

    public int getCurrentOpATen() {
        return currentOpATen;
    }

    public void setCurrentOpATen(int currentOpATen) {
        this.currentOpATen = currentOpATen;
    }

    public void decrementCurrentOpATen() {
        this.currentOpATen--;
    }

    public int getCurrentOpBTen() {
        return currentOpBTen;
    }

    public void setCurrentOpBTen(int currentOpBTen) {
        this.currentOpBTen = currentOpBTen;
    }

    public void decrementCurrentOpBTen() {
        this.currentOpBTen--;
    }

    public int getResultTen() {
        return resultTen;
    }

    public void setResultTen(int resultTen) {
        this.resultTen = resultTen;
    }

    public void incrementResultTen() {
        this.resultTen++;
    }

    public int getCurrentOpAOne() {
        return currentOpAOne;
    }

    public void setCurrentOpAOne(int currentOpAOne) {
        this.currentOpAOne = currentOpAOne;
    }

    public void decrementCurrentOpAOne() {
        this.currentOpAOne--;
    }

    public int getCurrentOpBOne() {
        return currentOpBOne;
    }

    public void setCurrentOpBOne(int currentOpBOne) {
        this.currentOpBOne = currentOpBOne;
    }

    public void decrementCurrentOpBOne() {
        this.currentOpBOne--;
    }

    public int getResultOne() {
        return resultOne;
    }

    public void setResultOne(int resultOne) {
        this.resultOne = resultOne;
    }

    public void incrementResultOne() {
        this.resultOne++;
    }

    public int getSubtrahendHun() {
        return subtrahendHun;
    }

    public void setSubtrahendHun(int subtrahendHun) {
        this.subtrahendHun = subtrahendHun;
    }

    public void incrementSubtrahendHun() {
        subtrahendHun++;
    }

    public int getSubtrahendTen() {
        return subtrahendTen;
    }

    public void setSubtrahendTen(int subtrahendTen) {
        this.subtrahendTen = subtrahendTen;
    }

    public void incrementSubtrahendTen() {
        subtrahendTen++;
    }

    public int getSubtrahendOne() {
        return subtrahendOne;
    }

    public void setSubtrahendOne(int subtrahendOne) {
        this.subtrahendOne = subtrahendOne;
    }

    public void incrementSubtrahendOne() {
        subtrahendOne++;
    }

    public int getMinuendHun() {
        return minuendHun;
    }

    public void setMinuendHun(int minuendHun) {
        this.minuendHun = minuendHun;
    }

    public int getMinuendTen() {
        return minuendTen;
    }

    public void setMinuendTen(int minuendTen) {
        this.minuendTen = minuendTen;
    }

    public int getMinuendOne() {
        return minuendOne;
    }

    public void setMinuendOne(int minuendOne) {
        this.minuendOne = minuendOne;
    }

    public boolean isCarrying() {
        return isCarrying;
    }

    public void setCarrying(boolean carrying) {
        isCarrying = carrying;
    }

    public boolean isBorrowing() {
        return isBorrowing;
    }

    public void setBorrowing(boolean borrowing) {
        isBorrowing = borrowing;
    }

    public boolean isHasBorrowedHun() {
        return hasBorrowedHun;
    }

    public void setHasBorrowedHun(boolean hasBorrowedHun) {
        this.hasBorrowedHun = hasBorrowedHun;
    }

    public boolean isHasBorrowedTen() {
        return hasBorrowedTen;
    }

    public void setHasBorrowedTen(boolean hasBorrowedTen) {
        this.hasBorrowedTen = hasBorrowedTen;
    }
}
