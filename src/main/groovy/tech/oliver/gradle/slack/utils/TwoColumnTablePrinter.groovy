package tech.oliver.gradle.slack.utils

class TwoColumnTablePrinter {

    private ArrayList<String> leftCol
    private int leftSize

    private ArrayList<String> rightCol
    private int rightSize

    private String title

    TwoColumnTablePrinter(String title) {
        leftCol = new ArrayList<String>()
        leftSize = 0

        rightCol = new ArrayList<String>()
        rightSize = 0

        this.title = title
    }

    void addRow(String left, String right) {
        leftCol.add(left)
        rightCol.add(right)

        leftSize = measureLargest(left, leftSize)
        rightSize = measureLargest(right, rightSize)
    }

    String getFormattedTable() {
        String centerDivider = '|'
        String cornerChar = '+'
        String headerChar = '='
        String endChar = '|'
        String rowChar = '-'

        StringBuilder builder = new StringBuilder()
        double overallSize = leftSize + rightSize + 5.0 // +1 for center divider, +4 for two spaces around each cell

        // Title
        int titleSpace = overallSize - title.length()
        int spaceLeft = Math.floor(titleSpace / 2.0)
        int spaceRight = Math.ceil(titleSpace / 2.0)

        String headerDividers = "${cornerChar}${headerChar * overallSize}${cornerChar}"
        String headerRow = "${endChar}${' ' * spaceLeft}${title}${' ' * spaceRight}${endChar}"

        builder.append(headerDividers)
        builder.append(System.getProperty('line.separator'))
        builder.append(headerRow)
        builder.append(System.getProperty('line.separator'))
        builder.append(headerDividers)
        builder.append(System.getProperty('line.separator'))

        // Rows

        String rowContent
        String rowDividers = "${cornerChar}${rowChar * overallSize}${cornerChar}"

        for(int i = 0; i < leftCol.size(); ++i) {
            spaceLeft = leftSize - leftCol[i].length()
            spaceRight = rightSize - rightCol[i].length()
            rowContent = "${endChar} ${leftCol[i]}${' ' * spaceLeft} ${centerDivider} ${rightCol[i]}${' ' * spaceRight} ${endChar}"

            builder.append(rowContent)
            builder.append(System.getProperty('line.separator'))
            builder.append(rowDividers)
            builder.append(System.getProperty('line.separator'))
        }

        return builder.toString()
    }

    private static int measureLargest(String value, int currentChamp) {
        int length = value.length()
        return length > currentChamp ? length : currentChamp
    }

}
