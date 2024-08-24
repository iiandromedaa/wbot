package com.androbohij.wbot.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SaveLoad {
    
    private static final File folder = new File("saves");

    private SaveLoad() {}

    /**
     * <p>saves an object (a map for example to store preferences for your module)
     * to the working directory of the bot in the java serialized object format
     * @param caller filenames are automated based on module name, plug in
     * here your module's class
     * @param toSerialize object to be serialized to file
     */
    public static void save(Class<?> caller, Object toSerialize) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File save = new File(folder, caller.getSimpleName() + "Stores.ser");
        try {
            FileOutputStream fos = new FileOutputStream(save);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(toSerialize);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>reads a file from the bot's working directory, deserializes the object
     * and returns it after casting it to a type determined by the user
     * @param caller filenames are automated based on module name, plug in
     * here your module's class
     * @param loadType what type you'd like the returned object to be cast to
     * (what class the object you saved was an instance of)
     * @return an object, cast to the type input in the loadType parameter
     */
    public static <T> T load(Class<?> caller, Class<T> loadType) {
        File save = new File(folder, caller.getSimpleName() + "Stores.ser");
        try {
            FileInputStream fis = new FileInputStream(save);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return loadType.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
