package ch.thoenluk.solvers.AndEquals1XorOrXor1;

class AndGate extends Gate {
    protected AndGate(final String firstInput, final String secondInput, final String operation, final String label) {
        super(firstInput, secondInput, operation, label);
    }

    @Override
    public Boolean apply(final Boolean first, final Boolean second) {
        return first && second;
    }

    @Override
    public boolean isAnd() {
        return true;
    }
}
