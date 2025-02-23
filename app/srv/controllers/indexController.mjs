export const fetchDateIndex = async (db) => {
  try {
    const collections = ['currencieIndex', 'dateIndex'];
    const results = {};

    for (const collectionName of collections) {
      const snapshot = await db.collection(collectionName).get();
      results[collectionName] = snapshot.docs.map((doc) => doc.id);
    }

    return results;
  } catch (error) {
    console.error('Error fetching indices:', error);
    throw error;
  }
};

