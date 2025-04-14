import java.io.*;
import java.util.*;

public class ConstraintValidator {

    // Job class
    static class Job {
        int r, d;
        public Job(int r, int d) { this.r = r; this.d = d; }
    }

    // Machine class
    static class Machine {
        int cost, capacity;
        public Machine(int cost, int capacity) { 
            this.cost = cost; 
            this.capacity = capacity; 
        }
    }
    
    //Batch class
    static class Batch {
        int time;        // tau(X)
        int machineType; // t(X)
        List<Integer> jobIds;
        public Batch(int time, int machineType, List<Integer> jobIds) {
            this.time = time;
            this.machineType = machineType;
            this.jobIds = jobIds;
        }
    }
    
    public static void main(String[] args) {
        // Loop from instance01.txt to instance99.txt
        for (int i = 1; i <= 99; i++) {
            String instanceFilename = String.format("instance%02d.txt", i);
            String solutionFilename = String.format("solution%02d.txt", i);
            System.out.println("Validating " + instanceFilename + " with " + solutionFilename);
            
            // If instance file doesn't exist, assume no further instances are available
            File instanceFile = new File(instanceFilename);
            if (!instanceFile.exists()) {
                System.out.println(instanceFilename + " not found. Terminating iteration.");
                break;
            }
            
            try {
                // Parse instance file
                List<Job> jobs = new ArrayList<>();
                List<Machine> machines = new ArrayList<>();
                Scanner instScanner = new Scanner(instanceFile);
                
                int n = instScanner.nextInt(); // number of jobs
                for (int j = 0; j < n; j++) {
                    int r = instScanner.nextInt();
                    int d = instScanner.nextInt();
                    jobs.add(new Job(r, d));
                }
                int K = instScanner.nextInt(); // number of machine types
                for (int j = 0; j < K; j++) {
                    int cost = instScanner.nextInt();
                    int cap = instScanner.nextInt();
                    machines.add(new Machine(cost, cap));
                }
                instScanner.close();
                
                // Parse the solution file if it exists
                File solutionFile = new File(solutionFilename);
                if (!solutionFile.exists()) {
                    System.out.println("Warning: " + solutionFilename + " does not exist. Skipping.");
                    continue;
                }
                List<Batch> batches = parseSolutionFile(solutionFile);
                
                // Check solution constraints
                boolean constraintsOk = validateConstraints(jobs, machines, batches);
                if (constraintsOk) {
                    System.out.println("Constraints are satisfied in " + solutionFilename);
                } else {
                    System.out.println("Constraint violations found in " + solutionFilename);
                }
                
            } catch (Exception ex) {
                System.out.println("Error while processing " + instanceFilename + ": " + ex.getMessage());
                ex.printStackTrace();
            }
            System.out.println("-------------------------------");
        }
    }

    // Parses the solution file and returns a list of batches.
    private static List<Batch> parseSolutionFile(File file) throws IOException {
        List<Batch> batches = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        if (line == null) {
            reader.close();
            throw new IOException("Solution file is empty.");
        }
        
        int numBatches;
        try {
            numBatches = Integer.parseInt(line.trim());
        } catch (NumberFormatException ex) {
            reader.close();
            throw new IOException("First line should be the number of batches.");
        }
        
        for (int i = 0; i < numBatches; i++) {
            line = reader.readLine();
            if (line == null) {
                reader.close();
                throw new IOException("Expected " + numBatches + " batch lines, but got fewer.");
            }
            // Each batch line: time machineType job1 job2 ... jobk
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length < 3) {
                System.out.println("Insufficient data in batch line: " + line);
                continue;
            }
            int time = Integer.parseInt(tokens[0]);
            int machineType = Integer.parseInt(tokens[1]);
            List<Integer> jobIds = new ArrayList<>();
            for (int j = 2; j < tokens.length; j++) {
                try {
                    jobIds.add(Integer.parseInt(tokens[j]));
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid job id in solution: " + tokens[j]);
                }
            }
            batches.add(new Batch(time, machineType, jobIds));
        }
        reader.close();
        return batches;
    }
    
    // Validates that the solution (batches) respects all given constraints.
    private static boolean validateConstraints(List<Job> jobs, List<Machine> machines, List<Batch> batches) {
        int n = jobs.size();
        boolean valid = true;
        boolean[] scheduled = new boolean[n];  // Tracks if each job (1-indexed) has been scheduled
        
        for (Batch batch : batches) {
            // Check that the machine type index is valid
            if (batch.machineType < 0 || batch.machineType >= machines.size()) {
                System.out.println("Invalid machine type " + batch.machineType + " in a batch.");
                valid = false;
                continue;
            }
            
            Machine machine = machines.get(batch.machineType);
            
            // Check capacity: the number of jobs in a batch must not exceed the machine's capacity.
            if (batch.jobIds.size() > machine.capacity) {
                System.out.println("Batch uses machine type " + batch.machineType + " with capacity " 
                                   + machine.capacity + " but has " + batch.jobIds.size() + " jobs.");
                valid = false;
            }
            
            // For each job in the batch, check the time constraint and ensure job is scheduled only once.
            for (int jobId : batch.jobIds) {
                if (jobId < 1 || jobId > n) {
                    System.out.println("Job id " + jobId + " is out of valid range 1.." + n);
                    valid = false;
                    continue;
                }
                Job job = jobs.get(jobId - 1);  // Convert 1-indexed to 0-indexed
                if (!(job.r <= batch.time && batch.time <= job.d)) {
                    System.out.println("Batch scheduled at time " + batch.time + " does not satisfy job " + jobId 
                                       + "'s time interval [" + job.r + ", " + job.d + "].");
                    valid = false;
                }
                if (scheduled[jobId - 1]) {
                    System.out.println("Job " + jobId + " is scheduled more than once.");
                    valid = false;
                }
                scheduled[jobId - 1] = true;
            }
        }
        
        // Check that every job has been scheduled.
        for (int i = 0; i < n; i++) {
            if (!scheduled[i]) {
                System.out.println("Job " + (i + 1) + " is not scheduled in any batch.");
                valid = false;
            }
        }
        return valid;
    }
}