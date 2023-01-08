class Student {
  constructor(rollNumber, name, age) {
    this.rollNumber = rollNumber;
    this.name = name;
    this.age = age;
  }
} //class ends

class StudentService {
  addStudent(student) {
    var prm = new Promise(function (resolve, reject) {
      $.ajax({
        url: "/mywebservice/schoolService/sstudentServices/aaddStudent",
        type: "POST",
        data: JSON.stringify(student),
        contentType: "application/json",
        success: function () {
          resolve("student data added");
        },
        error: function () {
          reject("student data not added");
        },
      });
    });
    return prm;
  } //addStudent function ends

  getAllStudent() {
    var prm = new Promise(function (resolve, reject) {
      $.ajax({
        url: "/mywebservice/schoolService/sstudentServices/getAllStudent",
        type: "GET",
        contentType: "application/json",
        success: function (responseData) {
          resolve(responseData);
        },
        error: function () {
          reject("response failed");
        },
      });
    });
    return prm;
  }
} //class ends
