package com.lyon.easy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Lyon
 */
public class RunTestt {


    public static class ConsumerChain<T> {

        private final List<ScoreConsumer<T>> chains = new ArrayList<>();

        public void add(ScoreConsumer<T> scoreConsumer) {
            chains.add(scoreConsumer);
        }

        public void exec(T data) {
            chains.forEach(scoreConsumer -> scoreConsumer.exec(data));
        }
    }

    private static final ConsumerChain<Integer> chain = new ConsumerChain<>();

    static {
        chain.add(new ScoreConsumer<>(score -> score > 50, System.out::println));
        chain.add(new ScoreConsumer<>(score -> score < 50, System.out::println));
        chain.add(new ScoreConsumer<>(score -> score == 50, System.out::println));
    }

    public static void main(String[] args) {
        int score = 50;
        chain.exec(score);
    }


    public static class ScoreConsumer<T> {
        /**
         *
         * @param predicate supports
         * @param consumer action
         */
        public ScoreConsumer(Predicate<T> predicate, Consumer<T> consumer) {
            this.predicate = predicate;
            this.consumer = consumer;
        }

        private final Predicate<T> predicate;
        private final Consumer<T> consumer;

        public void exec(T score) {
            if (predicate.test(score)) {
                consumer.accept(score);
            }
        }
    }

}
