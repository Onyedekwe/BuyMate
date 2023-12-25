package com.hemerick.buymate.Database;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hemerick.buymate.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.muddz.styleabletoast.StyleableToast;

public class Firebase {

    private static DatabaseReference databaseReference;
    private static DatabaseReference idRef;
    private static DatabaseReference shoppingListRef;
    ShopDatabase db;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    String email;
    Context context;

    public Firebase(Context context) {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            email = firebaseUser.getEmail();

        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        idRef = databaseReference.child(email.replace(".", "_"));
        shoppingListRef = idRef.child("shoppingLists");
        db = new ShopDatabase(context);
        this.context = context;
    }

    public void insertNewData(String category, String description, int status, String price, String month, String year, String day, String time, String quantity, int favourites, String photourl, String unit) {


        DatabaseReference newDataRef = shoppingListRef.child("list_category");
        DatabaseReference newItemRef = newDataRef.push();
        newItemRef.child("category").setValue(category);
        newItemRef.child("description").setValue(description);
        newItemRef.child("status").setValue(status);
        newItemRef.child("price").setValue(price);
        newItemRef.child("month").setValue(month);
        newItemRef.child("year").setValue(year);
        newItemRef.child("day").setValue(day);
        newItemRef.child("time").setValue(time);
        newItemRef.child("quantity").setValue(quantity);
        newItemRef.child("favourites").setValue(favourites);
        newItemRef.child("photourl").setValue(photourl);
        newItemRef.child("unit").setValue(unit);

    }

    public void insertNote(String heading, String content, String date) {
        DatabaseReference newDataRef = shoppingListRef.child("note_heading");
        DatabaseReference newNoteRef = newDataRef.push();
        newNoteRef.child("heading").setValue(heading);
        newNoteRef.child("content").setValue(content);
        newNoteRef.child("date").setValue(date);
    }

    public void updateItemName(String category, String description, String temp_description) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(temp_description)) {
                            Map<String, Object> updateItemName = new HashMap<>();
                            updateItemName.put("description", description);

                            itemSnapshot.getRef().updateChildren(updateItemName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }

    public void updateNote(String heading, String new_heading, String new_content, String new_date) {
        DatabaseReference categoryRef = shoppingListRef.child("note_heading");

        Query query = categoryRef.orderByChild("heading").equalTo(heading);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Map<String, Object> updateNote = new HashMap<>();
                        updateNote.put("heading", new_heading);
                        updateNote.put("content", new_content);
                        updateNote.put("date", new_date);

                        itemSnapshot.getRef().updateChildren(updateNote).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(context, "Category not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }

    public void updateCategoryName(String category, String temp_category) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(temp_category);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        itemSnapshot.getRef().child("category").setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(context, "Category not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });

    }


    public void moveItem(String temp_category, String category, String description) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(temp_category);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            itemSnapshot.getRef().child("category").setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Error moving item", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });

    }


    public void updateStatus(String category, String description, int status) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            Map<String, Object> updateItemName = new HashMap<>();
                            updateItemName.put("status", status);

                            itemSnapshot.getRef().updateChildren(updateItemName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }

    public void updateFavourites(String category, String description, int favourites) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            Map<String, Object> updateItemName = new HashMap<>();
                            updateItemName.put("favourites", favourites);

                            itemSnapshot.getRef().updateChildren(updateItemName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }

    public void updatePhotourl(String category, String description, String photourl) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            Map<String, Object> updateItemName = new HashMap<>();
                            updateItemName.put("photourl", photourl);

                            itemSnapshot.getRef().updateChildren(updateItemName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }

    public void updatePrice(String category, String description, String price) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            Map<String, Object> updateItemName = new HashMap<>();
                            updateItemName.put("price", price);

                            itemSnapshot.getRef().updateChildren(updateItemName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }


    public void updateQuantity(String category, String description, String quantity, String unit) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            Map<String, Object> updateItemName = new HashMap<>();
                            updateItemName.put("quantity", quantity);
                            updateItemName.put("unit", unit);

                            itemSnapshot.getRef().updateChildren(updateItemName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

    public void deleteItem(String category, String description) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("description").getValue(String.class).equals(description)) {
                            itemSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

    public void deleteCategory(String category) {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");

        Query query = categoryRef.orderByChild("category").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        itemSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

    public void deleteNote(String heading, String content) {
        DatabaseReference categoryRef = shoppingListRef.child("note_heading");

        Query query = categoryRef.orderByChild("heading").equalTo(heading);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        if (itemSnapshot.child("content").getValue(String.class).equals(content)) {
                            itemSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    } else {
                                        Toast.makeText(context, "Error: " + task.getResult(), Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }


    public void backupData() {
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<String> descriptions = new ArrayList<>();
        ArrayList<Integer> status = new ArrayList<>();
        ArrayList<String> price = new ArrayList<>();
        ArrayList<String> month = new ArrayList<>();
        ArrayList<String> year = new ArrayList<>();
        ArrayList<String> day = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> quantity = new ArrayList<>();
        ArrayList<Integer> favourites = new ArrayList<>();
        ArrayList<String> unit = new ArrayList<>();
        ArrayList<String> photourl = new ArrayList<>();


        ArrayList<String> heading = new ArrayList<>();
        ArrayList<String> content = new ArrayList<>();
        ArrayList<String> date = new ArrayList<>();

        Cursor res = db.getCategory(context);
        while (res.moveToNext()) {
            categories.add(res.getString(1));
            descriptions.add(res.getString(2));
            status.add(res.getInt(3));
            price.add(res.getString(4));
            month.add(res.getString(5));
            year.add(res.getString(6));
            day.add(res.getString(7));
            time.add(res.getString(8));
            quantity.add(res.getString(9));
            favourites.add(res.getInt(10));
            unit.add(res.getString(11));
            photourl.add(res.getString(12));
        }

        res = db.getNoteHeading();
        while (res.moveToNext()) {
            heading.add(res.getString(1));
            content.add(res.getString(2));
            date.add(res.getString(3));
        }

        res.close();

        for (int i = 0; i < categories.size(); i++) {

            insertNewData(categories.get(i), descriptions.get(i),
                    status.get(i), price.get(i), month.get(i),
                    year.get(i), day.get(i), time.get(i),
                    quantity.get(i), favourites.get(i), photourl.get(i), unit.get(i));
        }

        for (int j = 0; j < heading.size(); j++) {
            insertNote(heading.get(j), content.get(j), date.get(j));
        }

        StyleableToast.makeText(context, "Backup complete", R.style.custom_toast_2).show();


    }

    public void getAllList() {
        DatabaseReference categoryRef = shoppingListRef.child("list_category");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    ArrayList<String> total_url = new ArrayList<>();
                    Cursor res = db.getCategory(context);
                    while (res.moveToNext()) {
                        total_url.add(res.getString(12));
                    }
                    res.close();

                    for (int i = 0; i < total_url.size(); i++) {
                        if (!total_url.get(i).trim().isEmpty()) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageReference = storage.getReference().child(total_url.get(i));
                            storageReference.delete();
                        }
                    }

                    db.deleteAllList();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        String category = itemSnapshot.child("category").getValue(String.class);
                        String description = itemSnapshot.child("description").getValue(String.class);
                        int status = itemSnapshot.child("status").getValue(Integer.class);
                        String price = itemSnapshot.child("price").getValue(String.class);
                        String month = itemSnapshot.child("month").getValue(String.class);
                        String year = itemSnapshot.child("year").getValue(String.class);
                        String day = itemSnapshot.child("day").getValue(String.class);
                        String time = itemSnapshot.child("time").getValue(String.class);
                        String quantity = itemSnapshot.child("quantity").getValue(String.class);
                        int favourites = itemSnapshot.child("favourites").getValue(Integer.class);
                        String unit = itemSnapshot.child("unit").getValue(String.class);

                        db.insertItem(category, description, status, price, month, year, day, time, quantity, unit);
                        db.updateFavourites(category, description, favourites);

                    }
                } else {
                    Toast.makeText(context, "No list found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });

    }

    public void getAllNotes() {
        DatabaseReference categoryRef = shoppingListRef.child("note_heading");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    db.deleteAllNote();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        String heading = itemSnapshot.child("heading").getValue(String.class);
                        String content = itemSnapshot.child("content").getValue(String.class);
                        String date = itemSnapshot.child("date").getValue(String.class);

                        db.insertNote(heading, content, date);
                    }
                } else {
                    Toast.makeText(context, "No notes found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });

    }

    public void deleteData() {


        idRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT);
            }
        });


    }

}
