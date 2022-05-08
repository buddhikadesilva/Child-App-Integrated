package org.tensorflow.lite.examples.textclassification;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DAOAlert {
    private DatabaseReference databaseReference;
    public DAOAlert(){
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Alert.class.getSimpleName());
    }

    public Task<Void> add(Alert alert){
       return databaseReference.push().setValue(alert);
    }
}
