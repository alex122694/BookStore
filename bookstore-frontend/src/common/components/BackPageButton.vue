<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

defineProps({
    color: {
        type: String,
        default: 'grey-darken-1' // Default color
    }
});

const goBack = () => {
    router.back();
};
</script>

<template>
    <v-btn icon="mdi-arrow-left" variant="text" :color="color" class="mr-4" @click="goBack">
    </v-btn>
</template>
