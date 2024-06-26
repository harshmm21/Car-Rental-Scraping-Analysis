//package advancedanalysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import features.*;
import htmlparser.AvisBudgetParser;
import htmlparser.CarRentalParser;
import model.CarInfo;
import webcrawling.AvisCanadaCrawl;
import webcrawling.BudgetCanadaCrawl;
import webcrawling.CarRentalWebCrawl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public class ByteBuds {

    private static Hashtable<String, String> url_Map = new Hashtable<String, String>();
    private static String avisUrl = "https://www.avis.ca/en/home";
    private static String budgetUrl = "https://www.budget.ca/en/home";

    public static void main(String[] args) {

//        performCrawling();
//        List<CarInfo> carInfoList = performParsing();
//        filterCarDeals(carInfoList);


        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Select an option:");
            System.out.println("1. Perform Crawling");
            System.out.println("2. Perform Parsing");
            System.out.println("3. Exit");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    performCrawling();
                    break;

                case 2:
                    if (checkHtmlFiles()) {
                        List<CarInfo> carInfoList = performParsing();
                        filterCarDeals(carInfoList);
                    } else {
                        System.out.println("No HTML files available for parsing.");
                    }
                    break;

                case 3:
                    System.out.println("Exiting program. Goodbye!");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice. Please select again.");
            }
        }
    }

    private static boolean filesExistInAvis() {
        File avisFolder = new File("AvisFiles");

        if (avisFolder.exists() && avisFolder.isDirectory()) {
            File[] files = avisFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".html")) {
                        return true; // Found at least one HTML file
                    }
                }
            }
        }

        return false; // No HTML files found in the "AvisFiles" folder
    }

    private static boolean filesExistInBudget() {
        File avisFolder = new File("BudgetFiles");

        if (avisFolder.exists() && avisFolder.isDirectory()) {
            File[] files = avisFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".html")) {
                        return true; // Found at least one HTML file
                    }
                }
            }
        }

        return false; // No HTML files found in the "AvisFiles" folder
    }

    private static boolean filesExistInCarRental() {
        File avisFolder = new File("CarRentalFiles");

        if (avisFolder.exists() && avisFolder.isDirectory()) {
            File[] files = avisFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".html")) {
                        return true; // Found at least one HTML file
                    }
                }
            }
        }

        return false; // No HTML files found in the "AvisFiles" folder
    }

    private static boolean checkHtmlFiles() {
        // Check if there are HTML files in the specified folders (AvisFiles, BudgetFiles, CarRentalFiles)
        // You can implement the logic to check for files and return true if files are present, false otherwise.
        // For example:
        if (filesExistInAvis() || filesExistInBudget() || filesExistInCarRental()) {
            return true;
        } else {
            return false;
        }
        // Placeholder, replace with actual logic
    }


    private static void filterCarDeals(List<CarInfo> carInfoList) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("**************************************************");
        System.out.println("*                  BYTE BUDS                      *");
        System.out.println("*            CAR DEALS FILTER MENU                *");
        System.out.println("**************************************************");

        String refineSelection;
        do {
            System.out.println("Do you want to filter the car deals? (y/n): ");
            refineSelection = scanner.next().toLowerCase();
        } while (!DataValidation.validateUserResponse(refineSelection));

        List<CarInfo> processFilter = new ArrayList<>();
        while (refineSelection.equals("y")) {
//            processFilter.addAll(carInfoList);
            processFilter = carInfoList;
            processFilter.sort(Comparator.comparingDouble(CarInfo::getPrice));
            String option = "1";

            boolean validInput = false;

            while (!validInput) {
                try {
                    do {
                        System.out.println("\nSelect option to filter the deals:\n1. Display deals by price (LOW - HIGH)\n2. Sort by Car Name\n3. Car Price\n4. Sort by Transmission Type\n5. Sort by Passenger Capacity (HIGH - LOW)\n6. Sort by Luggage Capacity (HIGH - LOW)\n7. Show Car Count Analysis\n8. Exit");
                        option = scanner.next();
                    } while (!DataValidation.validateInteger(Integer.parseInt(option)));
                    validInput = true;
                }catch (NumberFormatException ex){
                    System.out.println("Invalid input. Please enter a valid response.");
                }
            }
            switch (Integer.parseInt(option)) {
                case 1:
//                    System.out.println("Enter preferred Car Name: ");
                    displayCarList(processFilter);
                    break;
                case 2:
                    System.out.println("The available Car Companies:");
                    Set<String> carList = carInfoList.stream()
                            .map(ele -> {
                                return ele.getName().split(" ")[0];
//                                String[] words = ele.getName().split(" ");
//                                return words.length >= 2 ? words[0] + " " + words[1] : ele.getName();
                            })
                            .collect(Collectors.toSet());


                    System.out.println(carList);

                    List<String> mostSearchedCars = SearchFrequency.displayMostSearchedCars(carList);

                    if (!mostSearchedCars.isEmpty()){
                        System.out.println("Most Searched Cars:");
                        for (String car : mostSearchedCars) {
                            System.out.println(car);
                        }
                    }

                    try {
                        SpellChecking.initializeDictionary("JsonData/filtered_car_deals.json");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    boolean check;
                    String preferredCarName;
                    do {
                        do {
                            System.out.println("Enter your preferred Car Company from above given list: ");
                            preferredCarName = scanner.next().toLowerCase();
                        } while (!DataValidation.validateCityName(preferredCarName));

                        check = SpellChecking.checkSpelling(preferredCarName);
                        if (!check) {
                            System.out.println("No such Car exists. Please try again any other from the given list...");
                        }
                    } while (!check);

                    SearchFrequency.incrementSearchFrequency(preferredCarName);

                    processFilter = filterByCarName(carInfoList, preferredCarName);
                    displayCarList(processFilter);

                    String s;
                    do {
                        System.out.println("Do you want to see Page Rank of websites for the given Car Model (Y/N): ");
                        s = scanner.next();
                    } while (!DataValidation.validateUserResponse(s));

                    if (s.equalsIgnoreCase("y")) {
                        try {
                            PageRanking.showRanking(preferredCarName);
//                            PageRanking.pageRank(preferredCarName);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case 3:
                    String preferredPriceRange;

                    do {
                        System.out.println("Enter preferred price range type (40-100): ");
                        preferredPriceRange = scanner.next().toLowerCase();
                    } while (!DataValidation.validateRangeInput(preferredPriceRange));

                    processFilter = filterByPriceRange(carInfoList, preferredPriceRange);
                    displayCarList(processFilter);
                    break;
                case 4:

                    String preferredTransmission;
                    do {
                        System.out.println("Enter preferred transmission type (A or M): ");
                        preferredTransmission = scanner.next().toUpperCase();
                    } while (!DataValidation.validateTTypes(preferredTransmission));

                    processFilter = switch (preferredTransmission.toUpperCase()) {
                        case "A" -> filterByTransmission(carInfoList, "Automatic");
                        case "M" -> filterByTransmission(carInfoList, "Manual");
                        default -> processFilter;
                    };
//                    processFilter = filterByTransmission(carInfoList, preferredTransmission);
                    displayCarList(processFilter);

                    break;
                case 5:
                    int preferredPassengerCapacity;
                    do {
                        System.out.println("Enter preferred passenger capacity: ");
                        preferredPassengerCapacity = scanner.nextInt();
                    } while (!DataValidation.validateInteger(Integer.parseInt(option)));
                    processFilter = filterByPassengerCapacity(carInfoList, preferredPassengerCapacity);
                    displayCarList(processFilter);

                    break;
                case 6:
                    int preferredLuggageCapacity;
                    do {
                        System.out.println("Enter preferred luggage capacity: ");
                        preferredLuggageCapacity = scanner.nextInt();
                    } while (!DataValidation.validateInteger(Integer.parseInt(option)));
                    processFilter = filterByLuggageCapacity(carInfoList, preferredLuggageCapacity);
                    displayCarList(processFilter);
                    break;
                case 7:
                    fetchCarAnalysis(carInfoList);
//                    processFilter = filterByLuggageCapacity(carInfoList, preferredLuggageCapacity);
                    break;
                case 8:
                    refineSelection = "no";
                    break;
                default:
                    System.out.println("Invalid option. Please enter a valid option.");
            }
        }
    }

    private static void fetchCarAnalysis(List<CarInfo> carInfoList) {
        Map<String, Integer> frequencyMap = FrequencyCount.getFrequencyCount("JsonData/filtered_car_deals.json");

        // Print the frequency count
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            System.out.println("Total Available \"" + entry.getKey() + "\" cars: \"" + entry.getValue() + "\"");
        }
    }

    private static List<CarInfo> filterByPriceRange(List<CarInfo> carInfoList, String preferredPriceRange) {
        String[] priceRange = preferredPriceRange.split("-");

        if (priceRange.length != 2) {
            // Handle invalid price range input
            throw new IllegalArgumentException("Invalid price range format");
        }

        int minPrice = Math.min(Integer.parseInt(priceRange[0].trim()),Integer.parseInt(priceRange[1].trim()));
        int maxPrice = Math.max(Integer.parseInt(priceRange[0].trim()),Integer.parseInt(priceRange[1].trim()));

        return carInfoList.stream()
                .filter(car -> {
                    try {
                        double carPrice = car.getPrice();
                        return carPrice >= minPrice && carPrice <= maxPrice;
                    } catch (NumberFormatException e) {
                        // Handle invalid price format for a car
                        return false;
                    }
                })
                .sorted(Comparator.comparingDouble(CarInfo::getPrice))
                .collect(Collectors.toList());
    }

    private static int getUserSelection(int maxOption) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select an option (or Enter 0 to select all): ");
        while (!scanner.hasNextInt()) {
            System.out.println("\nInvalid input. Please enter a valid option.");
            scanner.next();
        }
        int selectedOption = scanner.nextInt();
        return selectedOption;
    }

    private static List<CarInfo> filterByCarName(List<CarInfo> carInfoList, String preferredCarName) {
        try {
            SpellChecking.initializeDictionary("JsonData/filtered_car_deals.json");
            WordCompletion.initializeDictionaryFromJsonFile("JsonData/filtered_car_deals.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> suggestions = WordCompletion.getSuggestions(preferredCarName.toLowerCase());

        if (!suggestions.isEmpty()) {
            System.out.println("Suggestions:");

            for (int i = 0; i < suggestions.size(); i++) {

                System.out.println((i + 1) + ". " + suggestions.get(i));
            }

            // Assuming you have a method to get user input, e.g., getUserSelection
            int selectedOption = getUserSelection(suggestions.size());


            // Check if the selected option is valid
            if (selectedOption >= 1 && selectedOption <= suggestions.size()) {
                preferredCarName = suggestions.get(selectedOption - 1);
            } else if (selectedOption == 0) {
//                System.out.println(preferredCarName);
                // User selected all options, so no need to change preferredCarName
            } else {
                System.out.println("Invalid selection. Using original input.");
            }
        }

        try {
//            PageRanking2.showRanking(preferredCarName);
//            PageRanking.pageRank(preferredCarName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        if (!check) {
//        }

        String finalPreferredCarName = preferredCarName;
        return carInfoList.stream()
                .filter(car -> car.getName().equalsIgnoreCase(finalPreferredCarName) || car.getName().toLowerCase().contains(finalPreferredCarName))
                .sorted(Comparator.comparingDouble(CarInfo::getPrice))
                .toList();
    }

    private static List<CarInfo> filterByTransmission(List<CarInfo> carInfoList, String preferredTransmission) {
        return carInfoList.stream()
                .filter(car -> car.getTransmissionType().equalsIgnoreCase(preferredTransmission))
                .sorted(Comparator.comparingDouble(CarInfo::getPrice))
                .toList();
    }

    private static List<CarInfo> filterByPassengerCapacity(List<CarInfo> carInfoList, int preferredPassengerCapacity) {

        Optional<CarInfo> maxPassengerCapacity = carInfoList.stream()
                .max(Comparator.comparingInt(CarInfo::getPassengerCapacity));

        if (preferredPassengerCapacity > maxPassengerCapacity.get().getPassengerCapacity()) {
            preferredPassengerCapacity = maxPassengerCapacity.get().getPassengerCapacity();
        }
        int finalPreferredPassengerCapacity = preferredPassengerCapacity;

        return carInfoList.stream()
                .filter(car -> car.getPassengerCapacity() >= finalPreferredPassengerCapacity)
                .sorted(Comparator.comparingDouble(CarInfo::getPrice))
                .toList();
    }

    private static List<CarInfo> filterByLuggageCapacity(List<CarInfo> carInfoList, int preferredLuggageCapacity) {
//        Optional<CarInfo> maxLarge = carInfoList.stream().max(Comparator.comparingInt(CarInfo::getLargeBag));
//        Optional<CarInfo> maxSmall = carInfoList.stream().max(Comparator.comparingInt(CarInfo::getSmallBag));

        Optional<CarInfo> maxTotalCar = carInfoList.stream()
                .max(Comparator.comparingInt(car -> car.getLargeBag() + car.getSmallBag()));

        if (preferredLuggageCapacity > maxTotalCar.get().getLargeBag() + maxTotalCar.get().getSmallBag()) {
            preferredLuggageCapacity = maxTotalCar.get().getLargeBag() + maxTotalCar.get().getSmallBag();
        }
        int finalPreferredLuggageCapacity = preferredLuggageCapacity;
        return carInfoList.stream()
                .filter(car -> car.getLargeBag() + car.getSmallBag() >= finalPreferredLuggageCapacity)
                .sorted(Comparator.comparingDouble(CarInfo::getPrice))
                .toList();
    }

    private static void displayCarList(List<CarInfo> carInfoList) {
        // Display table header with borders
        System.out.println("+-------------------------+----------------------------------------+-------------------+------------------------+------------------------+--------------------------+--------------------+");
        System.out.println("|      Car Group          |          Car Model                     |    Rent Price     |   Passenger Capacity   |    Luggage Capacity    |     Transmission Type    |    Rental Company  |");
        System.out.println("+-------------------------+----------------------------------------+-------------------+------------------------+------------------------+--------------------------+--------------------+");

        // Display table rows with borders
        for (CarInfo carInfo : carInfoList) {
            System.out.printf("| %-23s | %-38s | $%-16.2f | %-22s | %-22s | %-24s | %-18s |\n",
                    carInfo.getCarGroup(), carInfo.getName(), carInfo.getPrice(),
                    carInfo.getPassengerCapacity(), carInfo.getLargeBag() + carInfo.getSmallBag(),
                    carInfo.getTransmissionType(), carInfo.getRentalCompany());
        }


        // Display table footer with borders
        System.out.println("+-------------------------+----------------------------------------+-------------------+------------------------+------------------------+--------------------------+--------------------+");
    }

    private static void saveCarInfoToJson(List<CarInfo> carInfoList, String filename) {
        ObjectMapper objectMapper = new ObjectMapper();
        String directoryPath = "JsonData/";

        try {
            File directory = new File(directoryPath);

            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create the file in the specified directory with the provided filename
            File file = new File(directory, filename + ".json");

//            System.out.println(carInfoList);
            // Write carInfoList to JSON file
            try {
                objectMapper.writeValue(file, carInfoList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//            System.out.println("Filtered car deals saved to: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<CarInfo> performParsing() {
        List<CarInfo> combined = new ArrayList<>();
        combined.addAll(AvisBudgetParser.parseFiles());
        combined.addAll(CarRentalParser.fetchAllCarRentalDeals());
        saveCarInfoToJson(combined, "filtered_car_deals");

        return combined;
    }

    private static void performCrawling() {
        AvisCanadaCrawl.initDriver();
        BudgetCanadaCrawl.initDriver();
        CarRentalWebCrawl.initDriver();

        Scanner scanner = new Scanner(System.in);
        String response;
        do {
            // Ask if pickup and drop-off locations are the same
            String sameLocationResponse;
            do {
                System.out.print("Are pickup and drop-off locations the same? (y/n): ");
                sameLocationResponse = scanner.nextLine().toLowerCase();
            } while (!DataValidation.validateUserResponse(sameLocationResponse));

            // Get pickup location
            String pickupLocation;
            do {
                System.out.print("Enter pickup location: ");
                pickupLocation = scanner.nextLine();
            } while (!DataValidation.validateCityName(pickupLocation));

            String finalSelectedPickupLoc = AvisCanadaCrawl.resolveLocation(pickupLocation, "PicLoc_value", "PicLoc_dropdown");
            BudgetCanadaCrawl.resolveLocation(finalSelectedPickupLoc.split(",")[0], "PicLoc_value", "PicLoc_dropdown");
//            CarRentalWebCrawl.handlePickUpLocation(finalSelectedPickupLoc);
            CarRentalWebCrawl.handlePickUpLocation(finalSelectedPickupLoc.split(",")[0]);
//            BudgetCanadaCrawl.resolveLocation(pickupLocation,"PicLoc_value", "PicLoc_dropdown");

//             Get drop-off location if locations are different
            String dropOffLocation;
            do {
                dropOffLocation = sameLocationResponse.equals("n") ? getDropOffLocation(scanner) : pickupLocation;
            } while (!DataValidation.validateCityName(dropOffLocation));

            if (sameLocationResponse.equals("n")){
                String finalSelectedDropOffLoc = AvisCanadaCrawl.resolveLocation(dropOffLocation, "DropLoc_value", "DropLoc_dropdown");
                BudgetCanadaCrawl.resolveLocation(finalSelectedDropOffLoc, "DropLoc_value", "DropLoc_dropdown");
                CarRentalWebCrawl.handleDropOffLocation(finalSelectedDropOffLoc);
            }


            // Get pickup date
            String pickupDate;
            do {
                System.out.print("Enter pickup date (DD/MM/YYYY): ");
                pickupDate = scanner.nextLine();
            } while (!DataValidation.validateDate(pickupDate));

            // Get drop-off date
            String returnDate;
            do {
                System.out.print("Enter return date (DD/MM/YYYY): ");
                returnDate = scanner.nextLine();
            } while (!DataValidation.validateDate(returnDate));

            CarRentalWebCrawl.resolveDate(pickupDate, returnDate);

            pickupDate = convertDateFormat(pickupDate);
            returnDate = convertDateFormat(returnDate);

            AvisCanadaCrawl.resolveDate(pickupDate, returnDate);
            BudgetCanadaCrawl.resolveDate(pickupDate, returnDate);

//            System.out.println("Do you have a specific time in mind to pick and return the car: ");

            String pickupTime;
            do {
                System.out.print("Enter pickup time (HH:MM AM/PM): ");
                pickupTime = scanner.nextLine();
            } while (!DataValidation.validateTime(pickupTime));

            // Get drop-off date
            String returnTime;
            do {
                System.out.print("Enter return time (HH:MM AM/PM): ");
                returnTime = scanner.nextLine();
            } while (!DataValidation.validateTime(returnTime));

            try {
                AvisCanadaCrawl.resolveTime(pickupTime, returnTime);
                BudgetCanadaCrawl.resolveTime(pickupTime, returnTime);
                CarRentalWebCrawl.resolveTime(pickupTime, returnTime);
                AvisCanadaCrawl.fetchCarDeals();
                BudgetCanadaCrawl.fetchCarDeals();
            }catch (Exception ex){

            }



            // Perform web scraping actions (replace with your actual scraping logic)
//            AvisCanadaWebCrawler avisCanadaWebCrawler = new AvisCanadaWebCrawler(driver,wait);
//            BudgetCanadaWebCrawler budgetCanadaWebCrawler = new BudgetCanadaWebCrawler(driver,wait);

//            url_Map.putAll(avisCanadaWebCrawler.scrapData(avisUrl, "Avis", "AvisHtml/", pickupLocation, dropOffLocation, pickupDate, returnDate));
//            url_Map.putAll(budgetCanadaWebCrawler.scrapData(budgetUrl, "Budget", "BudgetHtml/", pickupLocation, dropOffLocation, pickupDate, returnDate));

            // Ask if the user wants to continue
            System.out.print("Do you want to continue? (yes/no): ");
            response = scanner.nextLine();
            if (response.equalsIgnoreCase("yes")) {
                AvisCanadaCrawl.resetDriver();
                BudgetCanadaCrawl.resetDriver();
                CarRentalWebCrawl.resetDriver();
            }
        } while (response.equalsIgnoreCase("yes"));

        AvisCanadaCrawl.closeDriver();
        BudgetCanadaCrawl.closeDriver();
        CarRentalWebCrawl.closeDriver();
    }

    public static String convertDateFormat(String inputDate) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy");

        try {
            Date date = originalFormat.parse(inputDate);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace(); // Handle the ParseException as needed
            return null; // Return null or throw an exception based on your error handling strategy
        }
    }

    private static String getDropOffLocation(Scanner scanner) {
        System.out.print("Enter drop-off location: ");
        return scanner.nextLine();
    }
}