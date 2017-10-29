package st.photonbur.misc.image.misc;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an input the user has to give through a command prompt.
 * This class contains all methods to fully verify the input and return a proper result.
 * @param <TVal> The type the instances should have when validating
 * @param <TOut> The type the instances should have when actually being retrieved as results
 */
public class InputStep<TVal, TOut> {
    /**
     * The description shown when issued in the command prompt.
     */
    private final String desc;
    /**
     * The predicate handling validation of the given input.
     */
    private final Predicate<TVal> validator;
    /**
     * Handles conversion from scanner input to the format in which it should be validated.
     */
    private final Function<String, TVal> validationValueRetriever;
    /**
     * Handles conversion from scanner input to the format in which it should be returned as result.
     */
    private final Function<String, TOut> valueRetriever;

    /**
     * The internal copy of the user's parsed input.
     */
    private TOut result;

    public InputStep(String description,
                     Predicate<TVal> validator,
                     Function<String, TVal> validationValueRetriever,
                     Function<String, TOut> valueRetriever) {
        this.desc = description;
        this.validator = validator;
        this.validationValueRetriever = validationValueRetriever;
        this.valueRetriever = valueRetriever;
    }

    /**
     * @return The description shown when issued in the command prompt
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return The result of parsing the user's input by use of {@link #getValueRetriever()}
     */
    public TOut getResult() {
        return result;
    }

    /**
     * Sets the result of the parsing of this input step.
     * @param result The parsed result of the user's input
     */
    public void setResult(TOut result) {
        this.result = result;
    }

    /**
     * @return The handler of validating the user's input after use of {@link #getValidationValueRetriever()}
     */
    public Predicate<TVal> getValidator() {
        return validator;
    }

    /**
     * @return The handler of converting from scanner input to the format in which it will be validated
     */
    public Function<String, TVal> getValidationValueRetriever() {
        return validationValueRetriever;
    }

    /**
     * @return The handler of converting from scanner input to the format in which it should be returned as result
     */
    public Function<String, TOut> getValueRetriever() {
        return valueRetriever;
    }

}
