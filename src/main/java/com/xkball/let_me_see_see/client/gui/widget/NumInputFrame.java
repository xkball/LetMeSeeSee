package com.xkball.let_me_see_see.client.gui.widget;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ClampHorizontalPanel;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class NumInputFrame<T extends Number> extends ClampHorizontalPanel {
    
    @Nullable
    private Consumer<T> valueSetter;
    
    public NumInputFrame(boolean renderNumLabel) {
        this.setHorizontalAlign(HorizontalAlign.CENTER);
        this.setVerticalAlign(VerticalAlign.CENTER);
        this.setXMin(50);
        this.setYMin(20);
        this.setLeft(
                PanelConfig.ofFixSize(20, 20)
                        .paddingRight(8)
                        .apply(AutoResizeWidgetWrapper.of(Button.builder(Component.literal("-"), btn -> {
                            this.onDecreaseButtonClick();
                            this.update(IPanel.GLOBAL_UPDATE_MARKER);
                        }).build()))
        );
        this.setRight(
                PanelConfig.ofFixSize(20, 20)
                        .paddingLeft(8)
                        .apply(AutoResizeWidgetWrapper.of(Button.builder(Component.literal("+"), btn -> {
                            this.onIncreaseButtonClick();
                            this.update(IPanel.GLOBAL_UPDATE_MARKER);
                        }).build())));
        if (renderNumLabel) {
            this.addWidget(PanelConfig.of().trim()
                    .apply(Label.of(() -> Component.literal(String.valueOf(getValue())), 1.2f)));
        }
    }
    
    public NumInputFrame<T> setValueSetter(Consumer<T> valueSetter) {
        this.valueSetter = valueSetter;
        return this;
    }
    
    public void onValueChange() {
        if (valueSetter != null) {
            valueSetter.accept(getValue());
        }
    }
    
    public abstract void onDecreaseButtonClick();
    
    public abstract void onIncreaseButtonClick();
    
    public abstract @Nullable T getValue();
    
    public static class Pow2IntInput extends NumInputFrame<Integer> {
        private final int minN;
        private final int maxN;
        private int n;
        
        public Pow2IntInput(int minN, int maxN, int defaultN) {
            super(true);
            this.minN = minN;
            this.maxN = maxN;
            this.setN(defaultN);
        }
        
        @Override
        public void onDecreaseButtonClick() {
            var nn = Math.max(minN, n - 1);
            this.setN_(nn);
        }
        
        @Override
        public void onIncreaseButtonClick() {
            var nn = Math.min(n + 1, maxN);
            this.setN_(nn);
        }
        
        @Override
        public @Nullable Integer getValue() {
            return (int) Math.pow(2, n);
        }
        
        public int getN() {
            return n;
        }
        
        public void setN(int n) {
            var nn = Mth.clamp(n, minN, maxN);
            this.setN_(nn);
        }
        
        private void setN_(int nn) {
            if (nn == n) return;
            this.n = nn;
            this.onValueChange();
        }
    }
    
    public static class FloatInput extends NumInputFrame<Float> {
        
        private final float min;
        private final float max;
        private final float delta;
        @Nullable
        private Float value;
        @Nullable
        private ObjectInputBox<Float> input;
        
        public FloatInput(float min, float max, float delta, float default_, boolean renderNumLabel) {
            super(renderNumLabel);
            this.min = min;
            this.max = max;
            this.delta = delta;
            if (!renderNumLabel) {
                this.input = new ObjectInputBox<>(ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
                input.setOnLoseFocused(str -> this.setValue(Objects.requireNonNull(input).get()));
                this.addWidget(PanelConfig.of(1, 1)
                        .apply(AutoResizeWidgetWrapper.of(input)));
            }
            this.setValue(default_);
        }
        
        public void setValue(@Nullable Float value) {
            if (Objects.equals(this.value, value)) return;
            value = value == null ? null : Mth.clamp(value, min, max);
            if (input != null && value != null) {
                input.setValue(String.valueOf(value));
                input.displayPos = 0;
            }
            this.value = value;
            this.onValueChange();
        }
        
        @Override
        public void onDecreaseButtonClick() {
            if (value == null) {
                this.setValue(min);
            } else {
                this.setValue(Math.max(min, value - delta));
            }
        }
        
        @Override
        public void onIncreaseButtonClick() {
            if (value == null) {
                this.setValue(max);
            } else {
                this.setValue(Math.min(value + delta, max));
            }
        }
        
        @Override
        public @Nullable Float getValue() {
            return value;
        }
    }
    
}
