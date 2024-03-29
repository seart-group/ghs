package ch.usi.si.seart.repository.operation;

public sealed interface Operation permits UnaryOperation, BinaryOperation, TernaryOperation {

    String name();
}
