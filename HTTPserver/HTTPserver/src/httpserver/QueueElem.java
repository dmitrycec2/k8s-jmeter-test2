/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

/**
 *
 * @author 321
 */
class QueueElem {
    private final String val;
    private final Long checkTime;
    
    QueueElem(String val, long checkTime) {
        this.val = val;
        this.checkTime = checkTime;
    }

    boolean checkTime(long curTime) {
        return (curTime>=checkTime);
    }

    String getVal() {
        return val;
    }
    
}
