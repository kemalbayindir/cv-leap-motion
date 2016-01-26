package com.kemalbayindir.homeworks;

import com.kemalbayindir.homeworks.leap.LeapListener;
import com.leapmotion.leap.Controller;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by Kemal BAYINDIR on 1/5/2016.
 */

public class Main {

    public static void main(String[] args) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        LeapListener listener = new LeapListener();
        Controller controller = new Controller();
        controller.addListener(listener);
        controller.setPolicy(Controller.PolicyFlag.POLICY_IMAGES);

        System.out.println("Press enter for quit");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller.removeListener(listener);
    }

}
