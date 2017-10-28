package st.photonbur.misc.image;

import st.photonbur.misc.image.flow.FlowFrame;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ImageCreationFrameBase extends JFrame {
    private static final HashMap<String, Class<? extends ImageCreationFrameBase>> types;

    static {
        types = new HashMap<>();
        types.put("flow", FlowFrame.class);
    }

    protected abstract void exportImage() throws IOException;

    public abstract String getDefaultPreviewTitle();

    public static void main(String[] args) {
        Class<? extends ImageCreationFrameBase> target;

        if (args.length == 0) {
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("Type - one of:");
                types.keySet().forEach(type -> System.out.println(" - " + type));
                System.out.print(" > ");

                while (true) if (sc.hasNextLine()) break;

                target = findImageType(sc.nextLine());

                if (target != null) {
                    break;
                } else {
                    System.out.println("  [ERROR] - Invalid type entered!");
                }
            }
        } else {
            target = findImageType(args[0]);
            if (target == null) {
                System.out.println("  [ERROR] - Invalid type as argument!\n" + args[0]);
                System.exit(1);
            }
        }

        try {
            target.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private static Class<? extends ImageCreationFrameBase> findImageType(String search) {
        try {
            //noinspection ConstantConditions
            return types.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(search))
                    .findAny().get().getValue();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    protected void validateInput(ValidationStep... steps) {
        Scanner sc = new Scanner(System.in);

        for (ValidationStep step : steps) {
            while (true) {
                System.out.print(step.getDesc() + "\n > ");

                while (true) if (sc.hasNextLine()) break;

                try {
                    String input = sc.nextLine();

                    //noinspection unchecked
                    step.setResult(step.getValidator().test(step.getValidationValueRetriever().apply(input))
                            ? step.getValueRetriever().apply(input)
                            : null
                    );
                } catch (NumberFormatException ignored) { }

                if (step.getResult() != null) {
                    break;
                } else {
                    System.out.println("  [ERROR] - Invalid value entered!");
                }
            }
        }

        sc.close();
    }

    protected abstract void setupGUI();

    protected class ValidationStep<TIn, TOut> {
        private final String desc;
        private final Predicate<TIn> validator;
        private final Function<String, TIn> validationValueRetriever;
        private final Function<String, TOut> valueRetriever;

        private TOut result;

        public ValidationStep(String description,
                              Predicate<TIn> validator,
                              Function<String, TIn> validationValueRetriever,
                              Function<String, TOut> valueRetriever) {
            this.desc = description;
            this.validator = validator;
            this.validationValueRetriever = validationValueRetriever;
            this.valueRetriever = valueRetriever;
        }

        String getDesc() {
            return desc;
        }

        public TOut getResult() {
            return result;
        }

        void setResult(TOut result) {
            this.result = result;
        }

        Predicate<TIn> getValidator() {
            return validator;
        }

        Function<String, TIn> getValidationValueRetriever() {
            return validationValueRetriever;
        }

        Function<String, TOut> getValueRetriever() {
            return valueRetriever;
        }
    }
}
