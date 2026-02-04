import { configureStore } from '@reduxjs/toolkit';

import authReducer from '../reducers/authSlice'

const store = configureStore({
    reducer: {
        authStore: authReducer,
    },
    middleware: (getDefaultMiddleware) => {
        return getDefaultMiddleware({ serializableCheck: false });
    },
});

export default store;
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
