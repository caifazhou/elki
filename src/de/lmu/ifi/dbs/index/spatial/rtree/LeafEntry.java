package de.lmu.ifi.dbs.index.spatial.rtree;

import de.lmu.ifi.dbs.index.spatial.MBR;
import de.lmu.ifi.dbs.utilities.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The class LeafEntry represents an entry in a leaf node of a RTree.
 * A LeafEntry consists of a pair of id (representing the unique id
 * of the underlying data object) and the values
 * of the underlying data object.
 *
 * @author Elke Achtert (<a href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
class LeafEntry extends Entry {
  /**
   * The values of the underlying data object.
   */
  private double[] values;

  /**
   * Empty constructor for serialization purposes.
   */
  public LeafEntry() {
  }

  /**
   * Constructs a new LeafEntry object with the given parameters.
   *
   * @param id     the unique id of the underlying data object
   * @param values the values of the underlying data object
   */
  public LeafEntry(int id, double[] values) {
    super(id);
    this.values = values;
  }

  /**
   * Returns the values of the underlying data object of this entry.
   *
   * @return the values of the underlying data object of this entry
   */
  public double[] getValues() {
    return values;
  }

  /**
   * Returns the MBR of the underlying spatial object of this entry.
   *
   * @return the MBR of the underlying spatial object of this entry
   */
  MBR getMBR() {
    return new MBR(values, values);
  }

  /**
   * Returns the id as a string representation of this entry.
   *
   * @return a string representation of this entry
   */
  public String toString() {
    return "" + id + ", values " + Util.format(values);
  }

  /**
   * Calls the super and writes the values of
   * this entry to the specified output stream.
   *
   * @param out the stream to write the object to
   * @throws java.io.IOException Includes any I/O exceptions that may occur
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(values);
  }

  /**
   * Calls the super and reads the values of
   * this entry from the specified input stream.
   *
   * @param in the stream to read data from in order to restore the object
   * @throws java.io.IOException    if I/O errors occur
   * @throws ClassNotFoundException If the class for an object being
   *                                restored cannot be found.
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    this.values = (double[]) in.readObject();
  }

}
