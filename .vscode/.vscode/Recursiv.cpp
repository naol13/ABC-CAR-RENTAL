#include<iostream>
using namespace std;
int binarySearchRecursiv(int arr[],int target,int low, int high){
    if (low>high){
        return -1;
    }
    int mid=low+(high-low)/2;
    if (arr[mid]==target){
        return mid;
    }
    else if (arr[mid]<target){
        return  binarySearchRecursiv(arr, target, mid + 1, high);
    }
    else 
    {
      return binarySearchRecursiv(arr, target, low, mid - 1);  
    }

}
int main(){
    int arr[]={2,4,5,6,7,8,9,80};
    int size=sizeof(arr)/sizeof(arr[0]);
    int target =7;
    int result= binarySearchRecursiv( arr, target,0,size-1);

    if (result!= -1){
        cout<<"element found at index "<<result<<endl;
    }
    else {
        cout<<"the element not found "<<endl;
    }
    return 0;

}