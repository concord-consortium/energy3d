package org.concord.energy3d.model;

import com.ardor3d.math.type.ReadOnlyVector3;

public class PickedHousePart {

    private UserData userData;
    private ReadOnlyVector3 point;
    private ReadOnlyVector3 normal;

    public PickedHousePart(UserData userData, ReadOnlyVector3 point, ReadOnlyVector3 normal) {
        this.userData = userData;
        this.point = point;
        this.normal = normal;
    }

    public UserData getUserData() {
        return userData;
    }

    public ReadOnlyVector3 getPoint() {
        return point;
    }

    public ReadOnlyVector3 getNormal() {
        return normal;
    }

    public String toString() {
        return userData + " @" + point;
    }

}