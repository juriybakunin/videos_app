package tenet.lib.base;

public enum Err {
    OK,
    ERR_NO_AUTH,
    ERR_DATA_LOAD,
    ERR_ACCOUNT_BLOCKED,
    ERR_DEVICES_MAX_COUNT,
    ERR_LOGIN_NOT_FOUND,
    ERR_NOT_AVALIABLE,
    ERR_REPLACE_NOT_AVALIABLE,
    ERR_DEVICE_ARENDED,
    ERR_DEVICE_IN_USE,
    ERR_BAD_MAC_ADDR,
    ERR_ACCOUNT_ALREADY_USED,
    ERR_BAD_PIN_CODE,
    ERR_CANT_SUBSCRIBE,
    ERR_CANT_UNSUBSCRIBE;

    public boolean isOk(){
        return this == OK;
    }
}
