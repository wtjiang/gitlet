/**
 *
 */
package gitlet;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author winston
 */
public abstract class Serializer<T extends Serializable>
        implements Iterable<T> {

    /**
     * database name.
     */
    private static final String DB_NAME = "SERIAL";

    /** Base file object directory. */
    private Path directory;

    /**
     * is the serializer open.
     */
    private boolean open;

    /**
     * loaded objects.
     */
    private HashMap<String, Serializable> loadedMap;

    /**
     * tracked objects.
     */
    protected HashMap<Class<?>, Set<String>> tracker;


    /**
     * constructor.
     * @param base the base directory.
     */
    public Serializer(Path base) {
        this.directory = base;
        this.loadedMap = new HashMap<>();
        this.tracker = new LinkedHashMap<>();
        this.open = false;
    }


    /**
     * Gets a serial object.
     * @param type the type
     * @param fileName The file name of the object.
     * @param <S> the s
     * @return The object.
     */
    public <S extends T> S get(Class<S> type, String fileName) {
        try {
            Serializable obj = this.loadedMap.get(fileName);
            if (obj == null) {
                return this.load(type, fileName);
            } else {
                return type.cast(obj);
            }
        } catch (ClassCastException e) {
            String name = type.getSimpleName();
            throw new GitletException(
                    name + " as specified does not exist.");
        }
    }


    /**
     * Adds an object to the serial store.
     * @param file the file
     * @param toAdd to add
     * @param <S> s
     */
    public <S extends T> void add(String file, S toAdd) {

        if (this.loadedMap.get(file) != null
                || this.loadUnsafe(file) != null) {
            throw new GitletException(
                    "A " + toAdd.getClass().getSimpleName()
                            + " with that name already exists.");

        }
        this.loadedMap.put(file, toAdd);
        Set<String> tracked = this.tracker.get(toAdd.getClass());
        if (tracked == null) {
            tracked = new LinkedHashSet<String>();
            this.tracker.put(toAdd.getClass(), tracked);
        }
        tracked.add(file);
    }

    /**
     * Determines if the serializer contains a file.
     * @param file The file to check.
     * @return If it does contain the file.
     */
    public boolean contains(String file) {
        for (Class<?> type : this.tracker.keySet()) {
            if (this.contains(type, file)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Determines if the serializer contains a given file.
     * @param type the type to check./
     * @param file the file name.
     * @param <S> s
     * @return if it does
     */
    public <S extends T> boolean contains(Class<?> type, String file) {
        Set<String> files = this.tracker.get(type);
        if (files == null) {
            return false;
        }

        return files.contains(file);
    }



    /**
     * serialization.
     * @return true or false
     */
    protected abstract boolean niceSerialization();

    /**
     * Opens a serializer.
     */
    public void open() {
        this.open = true;

        if (!Files.exists(this.getDirectory())) {
            try {
                Files.createDirectories(this.getDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        HashMap<Class<?>, Set<String>> trck =
                (HashMap<Class<?>, Set<String>>) this.loadUnsafe(DB_NAME);

        if (trck == null) {
            this.loadedMap.put(DB_NAME, this.tracker);
        } else {
            this.tracker = trck;
        }

    }

    /**
     * Removes a file from the serializer.
     * @param type the type
     * @param file the file
     * @param <S> the s
     */
    public <S extends T> void remove(Class<S> type, String file) {
        try {
            Path filePath = this.directory.resolve(file);

            if (!this.tracker.containsKey(type)
                    || !this.tracker.get(type).contains(file)) {
                throw new GitletException(type.getSimpleName()
                        + " as specified does not exist.");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            this.tracker.get(type).remove(file);
            this.loadedMap.remove(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Returns if the serial manager is open.
     * @return If the manager is open.
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Gets an iterator for the lazy serial file manager.
     */
    @Override
    public Iterator<T> iterator() {
        List<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
        this.tracker.forEach((type, files) -> iterators.add(files.iterator()));

        List<Iterator<T>> fileIterators =
                iterators.stream().map(x -> this.new LoadingIterator(x))
                        .collect(Collectors.toList());

        return new Concaterator<>(fileIterators);
    }

    /**
     * Closes a repository and serializes every loaded object.
     */
    public void close() {
        if (this.isOpen()) {
            this.open = false;
            this.save(DB_NAME, this.tracker);
            this.loadedMap.forEach((file, obj) -> this.save(file, obj));

        }
    }

    /**
     * @return the directory
     */
    public Path getDirectory() {
        return this.directory;
    }


    /**
     * for each.
     * @param type type
     * @param action the action
     * @param <S> s
     */
    public <S extends T> void forEach(Class<S> type,
            final BiConsumer<? super String, ? super S> action) {
        if (!this.tracker.containsKey(type)) {
            String name = type.getSimpleName();
            throw new GitletException(
                    "No " + name.toLowerCase() + "s exist.");
        }
        this.tracker.get(type)
                .forEach(file -> action.accept(file, this.load(type, file)));
    }


    /**
     * Represents an iterator which loads files as they occur.
     * @author Winston
     */
    public class LoadingIterator implements Iterator<T> {
        /**
         * The file name iterator over which the loading iterator iterates.
         */
        private Iterator<String> fileNameIter;

        /**
         * Creates a loading iterator.
         * @param stringIter the file name iterator.
         */
        public LoadingIterator(Iterator<String> stringIter) {
            this.fileNameIter = stringIter;
        }

        @Override
        public boolean hasNext() {
            return this.fileNameIter.hasNext();
        }

        @Override
        public T next() {
            return Serializer.this.loadUnsafe(this.fileNameIter.next());
        }

    }


    /**
     * Loads a file unsafely.
     * @param file the file to load.
     * @return the loaded file.
     */
    @SuppressWarnings("unchecked")
    private T loadUnsafe(String file) {
        Path filePath = this.directory.resolve(file);
        try {
            InputStream fin = Files.newInputStream(filePath);
            ObjectInputStream oin = new ObjectInputStream(fin);

            Object unsafe;
            if (this.niceSerialization()) {
                XMLDecoder e = new XMLDecoder(oin);
                unsafe = e.readObject();
                e.close();
            } else {
                unsafe = oin.readObject();
            }

            T loaded = (T) unsafe;

            oin.close();
            fin.close();

            this.loadedMap.put(file, loaded);
            return loaded;

        } catch (IOException i) {
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * load an object.
     * @param type the type
     * @param file the file
     * @param <S> s
     * @return the load
     * @throws GitletException
     */
    private <S extends T> S load(Class<S> type, String file)
            throws GitletException {
        Path filePath = this.directory.resolve(file);
        try {
            InputStream fin = Files.newInputStream(filePath);
            ObjectInputStream oin = new ObjectInputStream(fin);

            Object dang;
            if (this.niceSerialization()) {
                XMLDecoder e = new XMLDecoder(oin);
                dang = e.readObject();
                e.close();
            } else {
                dang = oin.readObject();
            }

            S loaded = type.cast(dang);

            oin.close();
            fin.close();

            this.loadedMap.put(file, loaded);
            return loaded;

        } catch (IOException i) {
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Saves a serializable object object.
     * @param file the file name/relative path.
     * @param object the object to save.
     */
    private void save(String file, Object object) {
        Path filePath = this.directory.resolve(file);
        try {
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }
            OutputStream fin = Files.newOutputStream(filePath);
            ObjectOutputStream oin = new ObjectOutputStream(fin);
            if (this.niceSerialization()) {
                XMLEncoder e = new XMLEncoder(oin);
                e.writeObject(object);
                e.close();
            } else {
                oin.writeObject(object);
            }
            oin.close();
            fin.close();

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

}
