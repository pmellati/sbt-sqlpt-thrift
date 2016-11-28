#@namespace scala myorg.cars

//
// Random comments here ....
//

struct Car {
     1: required string car_id;           // Some comment.
     2: required string model;
        required i32    price = "PT";     // Default value gets ignored.
     4: optional bool   preowned;
}