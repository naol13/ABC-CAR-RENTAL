#include<iostream>
using namespace std;
int binarySearchItretive(int arr[],int size ,int target){
    int low =0;
    int high =size-1;
    while(low<=high){
        int mid=(low+high)/2;
        if(arr[mid]==target){
            return mid;
        }
        else if (arr[mid]<target){
            low=mid+1;   
        }
        else{
            high=mid-1;
        }

    }
    return -1;
}
 int main(){
    int arr[]={1,5,6,7,8,9,10,23,50};
    int size=sizeof(arr)/sizeof(arr[0]);
    int target=10;

    int result=binarySearchItretive( arr, size ,target);
    if(result !=-1){
        cout<<"element found at index "<<result<<endl;
}
    else
{
    cout<<"element not found"<<endl;

}
    return 0;
}
