package tenet.lib.base;

import java.io.Serializable;

/** Базовые интерфейсы */
public interface Interfaces {

    /** Интерфейс объекта, возвращающий имя в виде CharSequence */
    interface Named extends Interfaces,Serializable{
        CharSequence getName();
    }

    /** Интерфейс объекта, возвращающий имя и id */
    interface IdNamed extends Named {
        String getId();
    }

}
