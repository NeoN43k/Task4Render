package com.cgvsu.affine_transformations;

public enum Axis {
    X(0),
    Y(1),
    Z(2);
    private int number;
    Axis(int n){
        this.number = n;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(byte number) {
        this.number = number;
    }
}
