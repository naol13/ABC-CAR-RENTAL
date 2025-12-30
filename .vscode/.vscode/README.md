# ASSIGNMENT 1
## STUDENT INFORMATION
- **NAME** : NAOL SISAY
- **ID** : 0590/23
- **COURCE_NAME** : DSA
### ALGORITHIM : for smallest number in the array
1. Start with the first number in the list and assume it is the smallest
2. Compare it with the next number in the list.
3. If the next number is smallest, update the smallest number.
4. Repeat steps 2–3 for all numbers in the list.
5. When you reach the end of the list, the smallest number found is the result.
#### PSUEDOCODE

   Function smallestNumberInArray(arr[], size):

    Initialize minnum = arr[0]
    For i = 1 to size - 1:
        If arr[i] < minnum:
            minnum = arr[i]
    End For
    Return minnum

