import mongoose from 'mongoose';
import { DB_PORT, DB_NAME, DB_HOST} from './settings.js';


async function useDb() {
  return mongoose
    .connect(`mongodb://${DB_HOST}:${DB_PORT}/${DB_NAME}`)
    .catch(e => {
      console.log(e);
      process.exit(1);
    });
}

export default useDb;