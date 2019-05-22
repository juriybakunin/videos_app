package tenet.lib.base.types;

import tenet.lib.base.Interfaces;
import tenet.lib.base.utils.Utils;

public class BaseIdNamed<IMPL extends BaseIdNamed> implements Interfaces.IdNamed {
    public String id;
    public String name;

    protected IMPL getThis(){
        return (IMPL)this;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    /** Устанавливает id и имя из строки  ID::NAME */
    public IMPL setIdAndName(String idNameString){
        String s[] = idNameString.split("::");
        return setIdAndName(s[0],s[1]);
    }

    public IMPL setIdAndName(String id, String name){
        this.id = id;
        this.name = name;
        return getThis();
    }

    @Override
    public String toString() {
        return id + ' '+name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == getClass())
            return getId().equals(((IdNamed)obj).getId());
        return super.equals(obj);
    }

    public int findInIter(Iterable<IMPL> iter){
        return Utils.indexById(getId(),iter);
    }
}
