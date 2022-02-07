import java.io.*;
import java.util.Scanner;

/*
 * Prog1B.java -- This program was written to allow 
 * Random Access Files which are indexable and hold
 * records to be read and then searched by a key.
 * 
 * Given that the binary input file contains bytes 
 * and we know the byte size of the records it contains, 
 * we are easily able to index throughout the file, read
 * a certain amount of bytes to obtain a record object, 
 * and then print out specific information about the object.
 * 
 * We showcase this by printing out the first, middle, and last 
 * 5 records in the file as well as allowing the user to input 
 * key values to search for records in the file with ternary search.
 * 
 * Something to note is that we find the record length by 
 * reading the last 9 integers in the file which contain the 
 * length of 9 string fields in the record and adding those 
 * to the size of 4 integer fields in the record.
 * 
 * Op Reqs: The program compiles independently and requires
 * a command line argument pointing to a binary file holding 
 * records of a specific type. The input file con be contained 
 * in the same location as the program or elsewhere if a path 
 * is given as the command line argument.
 * 
 * Required features: Everything has been completed, the program 
 * correctly prints out the first, middle, and last five records 
 * or as many as it can and also allows for user search using 
 * ternary search.
 * 
 * Author: Alex Sava
 * Course: CSC 460 Database Design
 * Assignment: Program 1 Part B
 * Professor: Dr. McCann
 * TA's: Haris Riaz, Aayush Pinto
 * Due Date: 01/26/2022
*/
public class Prog1B {
    public static void main(String[] args) {
        File fileRef = null; // Reference to input file
        RandomAccessFile dataStream = null; // Reference to input stream of input file

        if (args.length == 0) { // Check to make sure file was provided as argument
            System.out.println("Error: .bin file required as input.");
            System.exit(-1);
        } else {
            try {
            fileRef = new File(args[0]);
            dataStream = new RandomAccessFile(fileRef, "rw");
            } catch (FileNotFoundException e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "creation of the RandomAccessFile object.");
            System.exit(-1);
            }
        }

        // Main program functions
        printRecordGroups(dataStream);
        userSearch(dataStream);
    }

    /*
     * static void getFieldLengths -- get the lengths of 
     * each string field of a record by reading information
     * stored at the end of an input file
     * 
     * @return: int[] -- array holding the lengths of 
     *  each string field in a specific order
     * @params: dataStream -- reference to binary file
     * to be read from
    */
    static int[] getFieldLengths(RandomAccessFile dataStream) {
        try {
            long fileLength = dataStream.length(); // Length of the input file
            dataStream.seek(fileLength - (9*4));
            int[] fieldLengths = {dataStream.readInt(), dataStream.readInt(), 
                dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), 
                dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), 
                dataStream.readInt()}; // Array used to store length information being read from the file
            return fieldLengths;
        } catch (IOException e) {
            System.out.println("I/O ERROR: Error accessing file to get field lengths.");
            System.exit(-1);
            return null;
        }
    }

    /*
     * static void getRecordLength -- get the total length 
     * of a single record
     * 
     * @return: in holding the length of a record
     * @params: fieldLengths -- integer array holding the 
     * lengths of each string field in the record
    */
    static int getRecordLength(int[] fieldLengths) {
        int recordLength = 4*4; // Initialize record size with 4 integers of 4 bytes
        for (int length: fieldLengths) {
            recordLength += length;
        }
        return recordLength;
    }

    /*
     * static void getNumRecords -- get the total number 
     * of records in the input file
     * 
     * @return: long holding the total number of records
     * @params: recordLength -- the length of a single 
     *  record in the file.
     * fileLength -- the total length of the file
    */
    static long getNumRecords(int recordLength, long fileLength) {
        return (fileLength - (9*4)) / recordLength;
    }

    /*
     * static void printRecordGroups -- prints the 
     * first, middle, and last 5 records in the input file
     * 
     * @return: none
     * @params: RandomAccessFile stream -- a reference to 
     *  the file being read from.
    */
    static void printRecordGroups(RandomAccessFile dataStream) {
        try {
            int[] fieldLengths = getFieldLengths(dataStream); // Lengths of string fields in records
            int recordLength = getRecordLength(fieldLengths); // Total length of a record
            long numberOfRecords = getNumRecords(recordLength, dataStream.length()); // Number of records in the file

            System.out.println("First five records");
            printFirstFive(dataStream, fieldLengths, numberOfRecords);

            System.out.println("Last five records");
            printLastFive(dataStream, fieldLengths, numberOfRecords, recordLength);

            System.out.println("Middle five records");
            printMiddleFive(dataStream, fieldLengths, numberOfRecords, recordLength);

            System.out.println("Number of Records");
            System.out.println(numberOfRecords);
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't get the file's length.");
            System.exit(-1);
        }
    }

    /*
     * static void ternarySearch -- performs recursive 
     * ternary search of the input file for a specific record
     * 
     * @return: int used to indicate record was found
     * @params: RandomAccessFile dataStream -- a reference to 
     *  the file being read from.
     * int[] fieldLengths -- the lengths of each 
     *  string field in the record. Used to create 
     *  correctly sized byte containers for the 
     *  data being read.
     * int recordLength -- the length of each record in the file.
     * int lowerBound -- the index of the lower bound in the search
     * int upperBound -- the index of the upper bound for the search
     * int val -- the value we are searching for
    */
    static int ternarySearch(RandomAccessFile dataStream, int[] fieldLengths, int recordLength, 
    int lowerBound, int upperBound, int val) {
        // Base case
        if (lowerBound > upperBound) {
            return -1;
        }

        int lowerDivider = lowerBound + (upperBound - lowerBound) / 3; // Lower value splitting "array" in three
        int upperDivider = upperBound - (upperBound - lowerBound) / 3; // Upper value splitting "array" in three

        DataRecord lowerRecord = new DataRecord(); // Record holding the information found at the lower divider
        DataRecord upperRecord = new DataRecord(); // Record holding the information found at the upper divider

        try {
            dataStream.seek(lowerDivider * recordLength);
            lowerRecord.fetchObject(dataStream, fieldLengths);
            if (lowerRecord.getCreditsIssued() == val) {
                System.out.println(lowerRecord);
                return 0;
            }

            dataStream.seek(upperDivider * recordLength);
            upperRecord.fetchObject(dataStream, fieldLengths);
            if (upperRecord.getCreditsIssued() == val) {
                System.out.println(upperRecord);
                return 0;
            }

            if (val < lowerRecord.getCreditsIssued()) {
                return ternarySearch(dataStream, fieldLengths, recordLength, lowerBound, lowerDivider - 1, val);
            } else if (val > upperRecord.getCreditsIssued()) {
                return ternarySearch(dataStream, fieldLengths, recordLength, upperDivider + 1, upperBound, val);
            } else {
                return ternarySearch(dataStream, fieldLengths, recordLength, lowerDivider + 1,  upperDivider - 1, val);
            }
        } catch (IOException e) {
            System.out.println("Error: Error during user search.");
            System.exit(-1);
        }
        return 0;
    }

    /*
     * static void userSearch -- search for a record in 
     * the input file with a credits issued value 
     * corresponding to the user input value
     * 
     * @return: none
     * @params: RandomAccessFile stream -- a reference to 
     *  the file being read from.
    */
    static void userSearch(RandomAccessFile dataStream) {
        try {
            int[] fieldLengths = getFieldLengths(dataStream); // Lengths of the fields of the records in the file
            int recordLength = getRecordLength(fieldLengths); // Total length of a record in the file
            long numRecords = getNumRecords(recordLength, dataStream.length()); // Number of records in the file

            Scanner inputReader = new Scanner(System.in); // Scanner used to read from std.in
            Scanner stringReader; // Scanner used to read strings

            // Ask for user input until -1 is used to exit
            while (true) {
                System.out.println("Search for records by credits issued:");
                String input = inputReader.nextLine(); // Grab the user input by line
                stringReader = new Scanner(input);
                try {
                    int userVal; // Current value from user to search with
                    // User can input multiple values so read them one at a time
                    while ((userVal = stringReader.nextInt()) != -1) {
                        if (ternarySearch(dataStream, fieldLengths, recordLength, 0, (int) numRecords - 1, userVal) == -1){
                            System.out.println("Search returned 0 records for criteria [Total Credits Issued: " 
                            + userVal + "]. Please restart search.");
                            break;
                        }
                    }
                    // Input of -1 is used to close the program
                    if (userVal == -1) {
                        inputReader.close();
                        stringReader.close();
                        System.out.println("Program shutting down.");
                        System.exit(0);
                    }
                } catch (Exception e) { // Catch is used incase user enters non integer value
                    continue;
                }
            }
        } catch (IOException e1) {
            System.out.println("Error: Error finding file meta data during user search.");
            System.exit(-1);
        }
    }

    /*
     * static void printFirstFive -- read and print up 
     * to five records from the beginning of the given 
     * binary file.
     * 
     * @return: none
     * @params: RandomAccessFile stream -- a reference to 
     *  the file being read from.
     * int[] stringLengths -- the lengths of each 
     *  string field in the record. Used to create 
     *  correctly sized byte containers for the 
     *  data being read.
     * long numRecords -- the number of records in the file.
    */
    static void printFirstFive (RandomAccessFile stream, int[] stringLengths, long numRecords) {
        // Make sure that we are indexed to the beginning of the file and then read up to 5 records
        try {
            stream.seek(0);
            int i = 0;
            while (i < 5 && i < numRecords) {
                DataRecord record = new DataRecord();
                record.fetchObject(stream, stringLengths);
                System.out.println(record);
                i++;
            }
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't get the first five objects.");
            System.exit(-1);
        }
    }

    /*
     * static void printLastFive -- read and print up 
     * to five records from the end of the given 
     * binary file.
     * 
     * @return: none
     * @params: RandomAccessFile stream -- a reference to 
     *  the file being read from.
     * int[] stringLengths -- the lengths of each 
     *  string field in the record. Used to create 
     *  correctly sized byte containers for the 
     *  data being read.
     * long numRecords -- the number of records in the file.
     * int recordLength -- the length of each record in the file.
    */
    static void printLastFive (RandomAccessFile stream, int[] stringLengths, long numRecords, int recordLength) {
        // If there are less than 5 records than start at the beginning of the file otherwise index to the last 5
        try {
            if (numRecords < 5) {
                stream.seek(0);
            } else {
                stream.seek(((int)numRecords - 5)* recordLength);
            }
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't get the last five objects.");
            System.exit(-1);
        }

        int i = 0;
        while (i < 5 && i < numRecords) {
            DataRecord record = new DataRecord();
            record.fetchObject(stream, stringLengths);
            System.out.println(record);
            i++;
        }
    }

    /*
     * static void printMiddleFive -- read and print up 
     * to five records from the middle of the given 
     * binary file.
     * 
     * @return: none
     * @params: RandomAccessFile stream -- a reference to 
     *  the file being read from.
     * int[] stringLengths -- the lengths of each 
     *  string field in the record. Used to create 
     *  correctly sized byte containers for the 
     *  data being read.
     * long numRecords -- the number of records in the file.
     * int recordLength -- the length of each record in the file.
    */
    static void printMiddleFive (RandomAccessFile stream, int[] stringLengths, long numRecords, int recordLength) {
        int startingIndex; // The index of the first of five middle records
        int printNum; // The number of records to be read and printed (4 for even 5 for odd)
        if (numRecords % 2 == 0) {
            startingIndex = (int) numRecords < 4 ? 0 : (int) numRecords/2 - 2;
            printNum = 4;
        } else {
            startingIndex = (int) numRecords < 5 ? 0 : (int) Math.floor((int) numRecords/2) - 2;
            printNum = 5;
        }

        try {
            stream.seek(startingIndex * recordLength);
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't get the middle five objects.");
            System.exit(-1);
        }

        int i = 0;
        while (i < printNum && i < numRecords) {
            DataRecord record = new DataRecord();
            record.fetchObject(stream, stringLengths);
            System.out.println(record);
            i++;
        }
    }
}

/*
 * DataRecord
 * Author: Alex Sava
 * 
 * This class was used in Prog1A to create and write 
 * DataRecords to a binary file. In Prog1B we are using 
 * the same class to hold records that we are reading 
 * from binary files created by Prog1A. The toString 
 * method is overloaded to make printing out specific 
 * information about the class simpler.
 * 
 * The following class variables are all fields that 
 * hold data from a csv file derived from the 
 * Voluntary Registry Offsets Database.
 * String projectId (id for the given project)
 * String projectName (name of the given project)
 * String status (current status of the given project)
 * String scope (project industry)
 * String type (project focus)
 * String methodology (project protocol)
 * String region (project region)
 * String country (project country)
 * String state (project state)
 * int creditsIssued (total carbon credits issued)
 * int creditsRetired (total carbon credits retired)
 * int creditsRemaining (total carbon credits remaining)
 * int firstYear (year of project start)
 * 
 * Uses default constructor with following methods:
 * String getProjectId()
 * String getProjectName()
 * String getProjectStatus()
 * String getProjectScope()
 * String getProjectType()
 * String getProjectMethodology()
 * String getProjectRegion()
 * String getProjectCountry()
 * String getProjectState()
 * int getCreditsIssued()
 * int getCreditsRemaining()
 * int getCreditsRetired()
 * int getFirstYear()
 * void setProjectId(val)
 * void setProjectName(val)
 * void setProjectStatus(val)
 * void setProjectScope(val)
 * void setProjectType(val)
 * void setProjectMethodology(val)
 * void setProjectRegion(val)
 * void setProjectCountry(val)
 * void setProjectState(val)
 * void setCreditsIssued(val)
 * void setCreditsRemaining(val)
 * void setCreditsRetired(val)
 * void setFirstYear(val)
 * void dumpObject(stream)
 * void fetchObject(stream, stringLengths)
 * String toString()
*/
class DataRecord {
    // Fields that make up a DataRecord
    private String projectId; // id of project in record
    private String projectName; // name of project in record
    private String status; // status of project in record
    private String scope; // industry of project in record
    private String type; // focus of project in record
    private String methodology; // protocol of project in record
    private String region; // region of project in record
    private String country; // country of project in record
    private String state; // state of project in record
    private int creditsIssued; // credits issued by project in record
    private int creditsRetired; // credits retired by project in record
    private int creditsRemaining; // credits remaining for project in record
    private int firstYear; // first year of project in record

    /*
     * "Getters" for class field values.
     * Each returns their respective field value 
     * to the caller.
    */
    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getStatus() { return status; }
    public String getScope() { return scope; }
    public String getType() { return type; }
    public String getMethodology() { return methodology; }
    public String getRegion() { return region; }
    public String getCountry() { return country; }
    public String getState() { return state; }
    public int getCreditsIssued() { return creditsIssued; }
    public int getCreditsRetired() { return creditsRetired; }
    public int getCreditsRemaining() { return creditsRemaining; }
    public int getFirstYear() { return firstYear; }

    /*
     * "Setters" for class field values
     * Each takes a value as input and replaces the value 
     * in their respective field with the input value.
    */
    public void setProjectId(String val) { projectId = val; }
    public void setProjectName(String val) { projectName = val; }
    public void setStatus(String val) { status = val; }
    public void setScope(String val) { scope = val; }
    public void setType(String val) { type = val; }
    public void setMethodology(String val) { methodology = val; }
    public void setRegion(String val) { region = val; }
    public void setCountry(String val) { country = val; }
    public void setState(String val) { state = val; }
    public void setCreditsIssued(int val) { creditsIssued = val; }
    public void setCreditsRetired(int val) { creditsRetired = val; }
    public void setCreditsRemaining(int val) { creditsRemaining = val; }
    public void setFirstYear(int val) { firstYear = val; }

    /*
     * public void dumpObject(RandomAccessFile stream) -- 
     * Writes the contents (fields) of the DataRecord to 
     * the output file given by the stream object reference.
     * 
     * @return: none
     * @params: RandomAccessFile stream -- a reference to 
     * the file being written to.
    */
    public void dumpObject(RandomAccessFile stream)
    {
        /*
         * Using the file reference methods to individually
         * write each class field to the file in bytes.
        */
        try {
            stream.writeBytes(projectId);
            stream.writeBytes(projectName);
            stream.writeBytes(status);
            stream.writeBytes(scope);
            stream.writeBytes(type);
            stream.writeBytes(methodology);
            stream.writeBytes(region);
            stream.writeBytes(country);
            stream.writeBytes(state);
            stream.writeInt(creditsIssued);
            stream.writeInt(creditsRetired);
            stream.writeInt(creditsRemaining);
            stream.writeInt(firstYear);
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't write to the file;\n\t"
                                + "perhaps the file system is full?");
            System.exit(-1);
        }
    }

    /*
     * public void fetchObject(RandomAccessFile stream, int[] stringLengths) -- 
     * Reads the contents in a binary file to store data into a DataRecord 
     * object.
     * 
     * @return: none
     * @params: 
     * RandomAccessFile stream -- a reference to 
     *  the file being read from.
     * int[] stringLengths -- the lengths of each 
     *  string field in the record. Used to create 
     *  correctly sized byte containers for the 
     *  data being read.
    */
    public void fetchObject(RandomAccessFile stream, int[] stringLengths) {
        // Byte arrays used to store field data read in as bytes from file
        byte[] idBytes = new byte[stringLengths[0]];
        byte[] nameBytes = new byte[stringLengths[1]];
        byte[] statusBytes= new byte[stringLengths[2]];
        byte[] scopeBytes = new byte[stringLengths[3]];
        byte[] typeBytes = new byte[stringLengths[4]];
        byte[] methodBytes = new byte[stringLengths[5]];
        byte[] regionBytes = new byte[stringLengths[6]];
        byte[] countryBytes = new byte[stringLengths[7]];
        byte[] stateBytes = new byte[stringLengths[8]];

        /*
         * Read 9 strings and 4 integers from the given 
         * file in order and store the string in their 
         * respective byte arrays while directly storing 
         * the ints in their respective fields. The byte 
         * arrays are then converted to strings and stored 
         * in their respective fields.
        */
        try {
            stream.readFully(idBytes);
            projectId = new String(idBytes);
            stream.readFully(nameBytes);
            projectName = new String(nameBytes);
            stream.readFully(statusBytes);
            status = new String(statusBytes);
            stream.readFully(scopeBytes);
            scope = new String(scopeBytes);
            stream.readFully(typeBytes);
            type = new String(typeBytes);
            stream.readFully(methodBytes);
            methodology = new String(methodBytes);
            stream.readFully(regionBytes);
            region = new String(regionBytes);
            stream.readFully(countryBytes);
            country = new String(countryBytes);
            stream.readFully(stateBytes);
            state = new String(stateBytes);
            creditsIssued = stream.readInt();
            creditsRetired = stream.readInt();
            creditsRemaining = stream.readInt();
            firstYear = stream.readInt();
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't read from the file;\n\t"
                            + "is the file accessible?");
            System.exit(-1);
        }
    }

    /*
     * public String toString() -- overrites the
     * toString method found in classes to allow for 
     * simple printing of object information.
     * 
     * @return: String made up of formatted object info.
     * @params: none
    */
    public String toString() {
        return "["+projectId+"]["+projectName+"]["+String.valueOf(creditsIssued)+"]";
    }
}