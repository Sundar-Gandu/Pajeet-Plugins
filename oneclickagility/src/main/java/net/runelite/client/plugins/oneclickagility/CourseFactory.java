package net.runelite.client.plugins.oneclickagility;

import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Arrays;

public class CourseFactory
{
    public static Course build(AgilityCourse course)
    {
        switch (course)
        {
            case DRAYNOR_VILLAGE:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3087,3108,3250,3278,0,11404),
                                new ObstacleArea(3097,3102,3277,3281,3,11405),
                                new ObstacleArea(3088,3092,3272,3276,3,11406),
                                new ObstacleArea(3089,3094,3265,3268,3,11430),
                                new ObstacleArea(3087,3088,3256,3261,3,11630),
                                new ObstacleArea(3087,3094,3255,3255,3,11631),
                                new ObstacleArea(3096,3101,3256,3261,3,11632)
                        ))
                );
            case AL_KHARID:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3268,3312,3158,3201,0,11633),
                                new ObstacleArea(3271,3278,3180,3192,3,14398),
                                new ObstacleArea(3265,3272,3161,3173,3,14402),
                                new ObstacleArea(3283,3302,3160,3176,3,14403),
                                new ObstacleArea(3313,3318,3160,3165,1,14404),
                                new ObstacleArea(3312,3318,3173,3179,2,11634),
                                new ObstacleArea(3312,3318,3180,3186,3,14409),
                                new ObstacleArea(3297,3306,3185,3194,3,14399)
                        ))
                );
            case VARROCK:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3190,3241,3407,3419,0,14412),
                                new ObstacleArea(3214,3220,3410,3419,3,14413),
                                new ObstacleArea(3201,3208,3414,3417,3,14414),
                                new ObstacleArea(3193,3197,3416,3416,1,14832),
                                new ObstacleArea(3192,3198,3402,3406,3,14833),
                                new ObstacleArea(3182,3208,3382,3404,3,14834),
                                new ObstacleArea(3218,3232,3393,3404,3,14835),
                                new ObstacleArea(3236,3240,3403,3409,3,14836),
                                new ObstacleArea(3236,3240,3410,3415,3,14841)
                        ))
                );
            case CANIFIS:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3480,3516,3471,3506,0,14843),
                                new ObstacleArea(3504,3510,3491,3498,2,14844),
                                new ObstacleArea(3496,3504,3503,3507,2,14845),
                                new ObstacleArea(3485,3493,3498,3505,2,14848),
                                new ObstacleArea(3474,3480,3491,3499,3,14846),
                                new ObstacleArea(3477,3484,3481,3487,2,14894),
                                new ObstacleArea(3488,3504,3468,3479,3,14847),
                                new ObstacleArea(3508,3517,3474,3483,2,14897)
                        ))
                );
            case FALADOR:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3020,3055,3331,3367,0,14898),
                                new ObstacleArea(3036,3040,3342,3343,3,14899),
                                new ObstacleArea(3044,3051,3341,3349,3,14901),
                                new ObstacleArea(3048,3050,3357,3358,3,14903),
                                new ObstacleArea(3045,3048,3361,3367,3,14904),
                                new ObstacleArea(3034,3041,3361,3364,3,14905),
                                new ObstacleArea(3026,3029,3352,3355,3,14911),
                                new ObstacleArea(3009,3021,3353,3358,3,14919),
                                new ObstacleArea(3016,3022,3343,3349,3,14920),
                                new ObstacleArea(3011,3015,3344,3346,3,14921),
                                new ObstacleArea(3009,3013,3335,3342,3,14922),
                                new ObstacleArea(3009,3013,3335,3342,3,14923),
                                new ObstacleArea(3012,3018,3331,3334,3,14924),
                                new ObstacleArea(3019,3024,3332,3335,3,14925)
                        ))
                );
            case SEERS_VILLAGE:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(2704,2733,3456,3495,0,14927),
                                new ObstacleArea(2721,2730,3490,3497,3,14928),
                                new ObstacleArea(2704,2714,3487,3497,2,14932),
                                new ObstacleArea(2709,2716,3476,3482,2,14929),
                                new ObstacleArea(2700,2715,3470,3475,3,14930),
                                new ObstacleArea(2690,2703,3459,3466,2,14931)
                        ))
                );
            case POLLNIVNEACH:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3345,3375,2957,3003,0,14935),
                                new ObstacleArea(3346,3351,2963,2968,1,14936),
                                new ObstacleArea(3352,3355,2973,2976,1,14937),
                                new ObstacleArea(3360,3362,2977,2979,1,14938),
                                new ObstacleArea(3366,3370,2974,2976,1,14939),
                                new ObstacleArea(3365,3369,2982,2986,1,14940),
                                new ObstacleArea(3355,3365,2980,2985,2,14941),
                                new ObstacleArea(3357,3370,2990,2995,2,14944),
                                new ObstacleArea(3356,3364,3000,3004,2,14945)
                        ))
                );
            case RELLEKKA:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(2622,2653,3656,3682,0,14946),
                                new ObstacleArea(2622,2626,3672,3676,3,14947),
                                new ObstacleArea(2615,2622,3658,3668,3,14987),
                                new ObstacleArea(2626,2630,3651,3655,3,14990),
                                new ObstacleArea(2639,2644,3649,3653,3,14991),
                                new ObstacleArea(2643,2650,3657,3662,3,14992),
                                new ObstacleArea(2655,2666,3665,3685,3,14994)
                        ))
                );
            case ARDOUGNE:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(2650,2674,3293,3319,0,15608),
                                new ObstacleArea(2671,2671,3299,3309,3,15609),
                                new ObstacleArea(2662,2665,3318,3318,3,26635),
                                new ObstacleArea(2654,2657,3318,3318,3,15610),
                                new ObstacleArea(2653,2653,3310,3314,3,15611),
                                new ObstacleArea(2651,2653,3300,3309,3,28912),
                                new ObstacleArea(2656,2656,3297,3297,3,15612)
                        ))
                );
            case PRIFDDINAS:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3239,3275,6102,6112,0,36221),
                                new ObstacleArea(3255,3257,6102,6112,2,36225),
                                new ObstacleArea(3272,3275,6105,6105,2,36227),
                                new ObstacleArea(3269,3269,6112,6115,2,36228),
                                new ObstacleArea(3268,3270,6116,6117,0,36229),
                                new ObstacleArea(2269,2273,3389,3394,0,36231),
                                new ObstacleArea(3267,3276,6140,6147,0,36232),
                                new ObstacleArea(2265,2269,3389,3393,2,36233),
                                new ObstacleArea(2254,2258,3386,3390,2,36234),
                                new ObstacleArea(2243,2247,3394,3398,2,36235),
                                new ObstacleArea(2244,2248,3405,3409,2,36236),
                                new ObstacleArea(2249,2253,3415,3419,2,36237),
                                new ObstacleArea(2253,2273,3422,3440,0,36238)
                        ))
                );
            case APE_ATOLL:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(2755,2772,2741,2755,0,15412),
                                new ObstacleArea(2751,2753,2741,2742,0,15414),
                                new ObstacleArea(2752,2753,2741,2742,2,15417),
                                new ObstacleArea(2747,2747,2741,2741,0,15483),
                                new ObstacleArea(2737,2753,2725,2741,0,15487),
                                new ObstacleArea(2755,2761,2727,2735,0,16062)
                        ))
                );
            case AGILITY_PYRAMID:
                return new PyramidCourse(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(3349,3380,2824,2858,0,10857),
                                new ObstacleArea(3354,3355,2833,2849,1,10865),
                                new ObstacleArea(3354,3365,2850,2852,1,10860),
                                new ObstacleArea(3368,3375,2845,2852,1,10868),
                                new ObstacleArea(3370,3375,2831,2840,1,10882),
                                new ObstacleArea(3362,3367,2831,2832,1,10886),
                                new ObstacleArea(3356,3359,2831,2834,1,10857),
                                new ObstacleArea(3356,3357,2835,2838,2,10884),
                                new ObstacleArea(3356,3357,2841,2848,2,10859),
                                new ObstacleArea(3356,3361,2849,2850,2,10861),
                                new ObstacleArea(3364,3373,2839,2850,2,10860),
                                new ObstacleArea(3370,3373,2833,2836,2,10865),
                                new ObstacleArea(3364,3369,2833,2834,2,10859),
                                new ObstacleArea(3358,3363,2833,2836,2,10857),
                                new ObstacleArea(3358,3359,2837,2839,3,10865),
                                new ObstacleArea(3358,3359,2840,2844,3,10888),
                                new ObstacleArea(3358,3371,2841,2848,3,10859),
                                new ObstacleArea(3370,3371,2835,2840,3,10868),
                                new ObstacleArea(3360,3365,2835,2838,3,10857),
                                new ObstacleArea(3040,3041,4695,4698,2,10859),
                                new ObstacleArea(3040,3042,4699,4702,2,10865),
                                new ObstacleArea(3043,3049,4695,4702,2,10859),
                                new ObstacleArea(3047,3049,4693,4694,2,10865),
                                new ObstacleArea(3042,3046,4693,4696,2,10857),
                                new ObstacleArea(3042,3047,4697,4700,3,10859),
                                new ObstacleArea(3044,3047,4695,4696,3,10855)
                        ))
                );
            case SHAYZIEN_BASIC:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(1520, 1557, 3624, 3641, 0, 42209),
                                new ObstacleArea(1553, 1555, 3631, 3634, 3, 42211),
                                new ObstacleArea(1538, 1542, 3630, 3635, 2, 42212),
                                new ObstacleArea(1527, 1529, 3632, 3634, 2, 42213),
                                new ObstacleArea(1522, 1524, 3643, 3645, 3, 42214),
                                new ObstacleArea(1538, 1539, 3643, 3645, 2, 42215),
                                new ObstacleArea(1552, 1556, 3643, 3644, 2, 42216)
                        ))
                );
            case SHAYZIEN_ADVANCED:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(1520, 1558, 3624, 3642, 0, 42209),
                                new ObstacleArea(1553, 1555, 3631, 3634, 3, 42211),
                                new ObstacleArea(1538, 1542, 3630, 3635, 2, 42212),
                                new ObstacleArea(1527, 1529, 3632, 3634, 2, 42217),
                                new ObstacleArea(1510, 1512, 3636, 3638, 2, 42218),
                                new ObstacleArea(1509, 1511, 3629, 3631, 2, 42219),
                                new ObstacleArea(1509, 1510, 3618, 3620, 2, 42220),
                                new ObstacleArea(1520, 1523, 3618, 3619, 2, 42221)
                        ))
                );
            case PENGUIN_COURSE:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new ObstacleArea(2652, 2664, 4032, 4041, 1, 21172),
                                new ObstacleArea(2633, 2651, 4032, 4056, 1,21126),
                                new SpecificObstacleArea(2627, 2635, 4052, 4065, 0,
                                        21120, new WorldPoint(2630, 4057, 0)),
                                new ObstacleArea(2630, 2630, 4057, 4057, 1,21126),
                                new ObstacleArea(2631, 2631, 4057, 4057, 1,21128),
                                new ObstacleArea(2631, 2631, 4059, 4059, 1,21129),
                                new ObstacleArea(2633, 2633, 4059, 4059, 1,21130),
                                new ObstacleArea(2635, 2635, 4059, 4059, 1,21131),
                                new ObstacleArea(2635, 2635, 4061, 4061, 1,21132),
                                new ObstacleArea(2635, 2635, 4063, 4063, 1,21133),
                                new SpecificObstacleArea(2633,2644,4065,4087,1,
                                        21134, new WorldPoint(2644, 4083, 1)),
                                new SpecificObstacleArea(2645,2652,4079,4087,1,
                                        21134, new WorldPoint(2652, 4080, 1)),
                                new SpecificObstacleArea(2653,2658,4078,4084,1,
                                        21134, new WorldPoint(2658, 4082, 1)),
                                new SpecificObstacleArea(2659,2662,4082,4086,1,
                                        21134, new WorldPoint(2662, 4082, 1)),
                                new ObstacleArea(2663, 2668, 4078, 4086, 1,21148),
                                new SpecificObstacleArea(2665,2665,4077,4077,1,
                                        21149,new WorldPoint(2665, 4076, 1)),
                                new SpecificObstacleArea(2665,2665,4076,4076,1,
                                        21150,new WorldPoint(2665, 4075, 1)),
                                new SpecificObstacleArea(2665,2665,4075,4075,1,
                                        21151,new WorldPoint(2665, 4074, 1)),
                                new SpecificObstacleArea(2665,2665,4074,4074,1,
                                        21152,new WorldPoint(2665, 4073, 1)),
                                new SpecificObstacleArea(2665,2665,4073,4073,1,
                                        21153,new WorldPoint(2665, 4072, 1)),
                                new SpecificObstacleArea(2665,2665,4072,4072,1,
                                        21153,new WorldPoint(2664, 4072, 1)),
                                new SpecificObstacleArea(2664,2664,4073,4073,1,
                                        21153,new WorldPoint(2664, 4072, 1)),
                                new SpecificObstacleArea(2664,2664,4072,4072,1,
                                        21154,new WorldPoint(2664, 4071, 1)),
                                new SpecificObstacleArea(2664,2664,4071,4071,1,
                                        21155,new WorldPoint(2664, 4070, 1)),
                                new SpecificObstacleArea(2664,2664,4070,4070,1,
                                        21156,new WorldPoint(2664, 4069, 1))
                        ))
                );
            case WEREWOLF_AGILITY:
                return new Course(
                        new ArrayList<>(Arrays.asList(
                                new SpecificObstacleArea(3538,3538,9873,9873,0,
                                        11643, new WorldPoint(3538, 9875, 0)),
                                new SpecificObstacleArea(3538,3538,9875,9875,0,
                                        11643, new WorldPoint(3538, 9877, 0)),
                                new SpecificObstacleArea(3538,3538,9877,9877,0,
                                        11643, new WorldPoint(3540, 9877, 0)),
                                new SpecificObstacleArea(3540,3540,9877,9877,0,
                                        11643, new WorldPoint(3540, 9879, 0)),
                                new SpecificObstacleArea(3540,3540,9879,9879,0,
                                        11643, new WorldPoint(3540, 9881, 0)),
                                new SpecificObstacleArea(3537,3544,9881,9892,0,
                                        11638, new WorldPoint(3540, 9893, 0)),
                                new SpecificObstacleArea(3537,3544,9894,9895,0,
                                        11638, new WorldPoint(3540, 9896, 0)),
                                new SpecificObstacleArea(3537,3544,9897,9898,0,
                                        11638, new WorldPoint(3540, 9899, 0)),
                                new SpecificObstacleArea(3537,3544,9900,9904,0,
                                        11657, new WorldPoint(3541, 9905, 0)),
                                new StickObstacleArea(3533,3546,9909,9915,0,
                                        11641, new WorldPoint(3532, 9910, 0)),
                                new ObstacleArea(3526,3532,9907,9912,0,11644),
                                new WerewolfObstacleArea()
                        ))
                );
            default:
                return null;

        }

    }
}
