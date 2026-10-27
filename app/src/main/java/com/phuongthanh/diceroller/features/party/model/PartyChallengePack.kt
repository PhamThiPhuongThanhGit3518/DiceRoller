package com.phuongthanh.diceroller.features.party.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.phuongthanh.diceroller.R

enum class ChallengeType(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int,
    @get:DrawableRes val iconRes: Int,
    val colorHex: String
) {
    FUNNY_QUESTIONS(
        R.string.type_funny_questions,
        R.string.type_funny_desc,
        R.drawable.ic_type_funny,
        "#10B981"
    ),
    QUICK_DARES(
        R.string.type_quick_dares,
        R.string.type_quick_desc,
        R.drawable.ic_type_dare,
        "#F59E0B"
    ),
    GROUP_ACTIONS(
        R.string.type_group_actions,
        R.string.type_group_desc,
        R.drawable.ic_type_group,
        "#3B82F6"
    ),
    RANDOM_TASKS(
        R.string.type_random_tasks,
        R.string.type_random_desc,
        R.drawable.ic_type_random,
        "#8B5CF6"
    )
}

data class PartyChallenge(
    val id: String,
    val type: ChallengeType,
    val text: String
)

data class PartyChallengePack(
    val id: String,
    val name: String,
    val description: String,
    val challengesCount: Int,
    val playersRange: String,
    val difficulty: String,
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList(),
    @get:DrawableRes val iconRes: Int = R.drawable.ic_dice_d6,
    val challenges: List<PartyChallenge>
)

object PartyPacksBank {

    private val classicPartyChallenges = listOf(
        // Funny Questions
        PartyChallenge("cp_1", ChallengeType.FUNNY_QUESTIONS, "Điều ngốc nghếch nhất bạn từng làm khi say là gì? / What's the silliest thing you did while drunk?"),
        PartyChallenge("cp_2", ChallengeType.FUNNY_QUESTIONS, "Ai là crush đầu tiên của bạn thời đi học? / Who was your very first school crush?"),
        PartyChallenge("cp_3", ChallengeType.FUNNY_QUESTIONS, "Bí mật ngớ ngẩn nhất bạn chưa từng kể với ai trong nhóm? / What is your silliest secret?"),
        PartyChallenge("cp_4", ChallengeType.FUNNY_QUESTIONS, "Nếu được đổi cuộc sống với 1 người trong bàn trong 24 giờ, bạn chọn ai? / Who would you swap lives with for 24h?"),
        PartyChallenge("cp_5", ChallengeType.FUNNY_QUESTIONS, "Thói quen kỳ quặc nhất của bạn khi ở nhà một mình là gì? / What weird habit do you have when alone?"),
        PartyChallenge("cp_6", ChallengeType.FUNNY_QUESTIONS, "Lần nói dối trắng trợn gần nhất của bạn là gì và với ai? / What was your most recent white lie?"),
        PartyChallenge("cp_7", ChallengeType.FUNNY_QUESTIONS, "Nếu có 1 triệu đô la ngay bây giờ, bạn sẽ mua thứ ngớ ngẩn nào đầu tiên? / What ridiculous thing would you buy first with $1M?"),
        PartyChallenge("cp_8", ChallengeType.FUNNY_QUESTIONS, "Biệt danh hài hước nhất mà bố mẹ hoặc bạn bè từng đặt cho bạn? / What is the funniest nickname you ever had?"),

        // Quick Dares
        PartyChallenge("cp_9", ChallengeType.QUICK_DARES, "Hát một đoạn điệp khúc bằng giọng sóc chuột cực dễ thương! / Sing a chorus in a chipmunk voice!"),
        PartyChallenge("cp_10", ChallengeType.QUICK_DARES, "Bắt chước biểu cảm hoặc dáng đi của người bên trái trong 15 giây! / Imitate the person on your left for 15s!"),
        PartyChallenge("cp_11", ChallengeType.QUICK_DARES, "Làm mặt xấu nhất trần đời và giữ nguyên 10 giây để mọi người chụp ảnh! / Make your funniest face for 10s!"),
        PartyChallenge("cp_12", ChallengeType.QUICK_DARES, "Đọc to tin nhắn gần nhất bạn nhận được trong điện thoại! / Read aloud the last text message you received!"),
        PartyChallenge("cp_13", ChallengeType.QUICK_DARES, "Nói chuyện bằng giọng người máy trong 2 vòng tiếp theo! / Speak like a robot for the next 2 rounds!"),
        PartyChallenge("cp_14", ChallengeType.QUICK_DARES, "Tự tin nhảy một điệu nhảy ngẫu hứng trong 15 giây! / Dance freestyle energetically for 15 seconds!"),
        PartyChallenge("cp_15", ChallengeType.QUICK_DARES, "Khen ngợi chân thành 3 người xung quanh bằng 3 từ hoa mỹ! / Sincerely compliment 3 people around you!"),

        // Group Actions
        PartyChallenge("cp_16", ChallengeType.GROUP_ACTIONS, "Cả bàn cùng hô vang 'Zôôô' và cụng ly / chạm tay thật to! / Everyone cheer and high five together!"),
        PartyChallenge("cp_17", ChallengeType.GROUP_ACTIONS, "Tất cả mọi người cùng giơ ngón tay chỉ vào người hài hước nhất bàn! / Everyone point to the funniest person!"),
        PartyChallenge("cp_18", ChallengeType.GROUP_ACTIONS, "Chơi trò 'Không chớp mắt' với người đối diện trong 20 giây! / Staring contest with the person opposite for 20s!"),
        PartyChallenge("cp_19", ChallengeType.GROUP_ACTIONS, "Cùng nhau hát đồng thanh một bài hát thiếu nhi quen thuộc! / Sing a well-known childhood song together!"),
        PartyChallenge("cp_20", ChallengeType.GROUP_ACTIONS, "Mỗi người lần lượt kể tên 1 món ăn trong 3 giây, ai trễ bị phạt! / Name a food in 3 seconds around the table!"),
        PartyChallenge("cp_21", ChallengeType.GROUP_ACTIONS, "Cả nhóm tạo dáng chụp chung một bức ảnh ngầu nhất có thể! / Group pose for the coolest photo possible!"),
        PartyChallenge("cp_22", ChallengeType.GROUP_ACTIONS, "Người vừa gieo chọn 2 người bất kỳ đổi chỗ ngồi cho nhau! / The roller swaps seats of two players!"),

        // Random Tasks
        PartyChallenge("cp_23", ChallengeType.RANDOM_TASKS, "Đố mẹo: Kể một câu chuyện cười ngắn, nếu không ai cười bạn bị phạt! / Tell a quick joke, if nobody laughs you get penalized!"),
        PartyChallenge("cp_24", ChallengeType.RANDOM_TASKS, "Gửi một biểu tượng cảm xúc ngẫu nhiên cho người thứ 5 trong danh sách chat! / Send a random emoji to contact #5!"),
        PartyChallenge("cp_25", ChallengeType.RANDOM_TASKS, "Đứng thăng bằng trên 1 chân trong 20 giây mà không chao đảo! / Balance on one foot for 20 seconds!"),
        PartyChallenge("cp_26", ChallengeType.RANDOM_TASKS, "Uống một ngụm nước mà không được dùng tay chạm vào ly! / Sip a drink without using your hands!"),
        PartyChallenge("cp_27", ChallengeType.RANDOM_TASKS, "Bịt mắt và đoán tên một người trong bàn chỉ bằng cách chạm vào tay! / Guess a player with eyes closed by touching their hand!"),
        PartyChallenge("cp_28", ChallengeType.RANDOM_TASKS, "Diễn tả tên một bộ phim bằng hành động không nói tiếng nào cho bàn đoán! / Charades: Act out a movie title silently!"),
        PartyChallenge("cp_29", ChallengeType.RANDOM_TASKS, "Tự thú nhận một tật xấu mà bạn muốn sửa nhưng chưa làm được! / Confess a bad habit you want to break!"),
        PartyChallenge("cp_30", ChallengeType.RANDOM_TASKS, "Hãy là DJ và chọn một bài hát cực sôi động cho cả phòng! / Be the DJ and play the most hype song right now!")
    )

    private val drinkingNightChallenges = listOf(
        // Funny Questions
        PartyChallenge("dn_1", ChallengeType.FUNNY_QUESTIONS, "Ai trong bàn này uống khỏe nhất và ai gục đầu tiên? / Who holds their drink best and who taps out first?"),
        PartyChallenge("dn_2", ChallengeType.FUNNY_QUESTIONS, "Trải nghiệm 'say không nhớ đường về' đáng nhớ nhất của bạn? / Your most memorable crazy night out story?"),
        PartyChallenge("dn_3", ChallengeType.FUNNY_QUESTIONS, "Bạn từng nhắn tin nhầm cho người yêu cũ lúc ngà ngà chưa? / Ever drunk-texted an ex?"),
        PartyChallenge("dn_4", ChallengeType.FUNNY_QUESTIONS, "Món đồ nhảm nhí nhất bạn từng mua lúc đang hưng phấn? / The dumbest purchase you made late at night?"),
        PartyChallenge("dn_5", ChallengeType.FUNNY_QUESTIONS, "Nếu bị kẹt trên hoang đảo với 1 người trong bàn, bạn chọn ai để sống sót? / Pick one person here to survive on an island!"),
        PartyChallenge("dn_6", ChallengeType.FUNNY_QUESTIONS, "Khoảnh khắc ngượng ngùng nhất khi đi bar / pub của bạn? / Your most embarrassing party/bar story?"),
        PartyChallenge("dn_7", ChallengeType.FUNNY_QUESTIONS, "Ai là người có gu ăn mặc đẹp nhất tối nay? / Who has the best outfit tonight?"),

        // Quick Dares
        PartyChallenge("dn_8", ChallengeType.QUICK_DARES, "Uống cạn nửa ly cùng người ngồi bên tay phải bạn! / Cheers & drink half a cup with the person on your right!"),
        PartyChallenge("dn_9", ChallengeType.QUICK_DARES, "Uống 1 ngụm theo phong cách quý tộc sang chảnh! / Take a sip in the most aristocratic royal style!"),
        PartyChallenge("dn_10", ChallengeType.QUICK_DARES, "Chỉ định một người bất kỳ trong bàn uống cùng bạn! / Pick anyone at the table to drink with you!"),
        PartyChallenge("dn_11", ChallengeType.QUICK_DARES, "Uống 1 hớp chéo tay với người bên trái! / Drink cross-armed with the person to your left!"),
        PartyChallenge("dn_12", ChallengeType.QUICK_DARES, "Nói câu 'Nâng ly vì tình bạn' bằng 3 ngôn ngữ khác nhau! / Say 'Cheers to friendship' in 3 languages!"),
        PartyChallenge("dn_13", ChallengeType.QUICK_DARES, "Bắt chước một bartender đang pha chế đồ uống chuyên nghiệp! / Pretend to be a pro bartender mixing drinks!"),
        PartyChallenge("dn_14", ChallengeType.QUICK_DARES, "Ai đeo kính trong bàn cùng nâng ly chúc mừng! / Everyone wearing glasses takes a sip!"),

        // Group Actions
        PartyChallenge("dn_15", ChallengeType.GROUP_ACTIONS, "Tất cả mọi người cùng nâng ly và uống 1 ngụm thật hào sảng! / Everyone raise your glasses and drink together!"),
        PartyChallenge("dn_16", ChallengeType.GROUP_ACTIONS, "Những ai chưa có người yêu uống 1 ngụm giải sầu! / Everyone currently single takes a sip!"),
        PartyChallenge("dn_17", ChallengeType.GROUP_ACTIONS, "Cả bàn chơi nối chữ nhanh, người thua uống 1 ngụm! / Fast word chain game, loser takes a sip!"),
        PartyChallenge("dn_18", ChallengeType.GROUP_ACTIONS, "Ai sinh vào mùa hè cùng chạm ly nào! / Summer babies raise your glasses and drink!"),
        PartyChallenge("dn_19", ChallengeType.GROUP_ACTIONS, "Người cao nhất và người thấp nhất trong bàn nâng ly giao lưu! / Tallest and shortest players toast each other!"),
        PartyChallenge("dn_20", ChallengeType.GROUP_ACTIONS, "Cùng hô to khẩu hiệu của nhóm 3 lần! / Shout the group's party motto 3 times!"),
        PartyChallenge("dn_21", ChallengeType.GROUP_ACTIONS, "Ai đang dùng iPhone thì uống 1 ngụm! / Everyone using an iPhone takes a sip!"),
        PartyChallenge("dn_22", ChallengeType.GROUP_ACTIONS, "Ai đang dùng Android thì nâng ly chúc mừng sự tiện lợi! / Android users raise glasses and cheers!"),

        // Random Tasks
        PartyChallenge("dn_23", ChallengeType.RANDOM_TASKS, "Rót đầy đồ uống cho người bên trái một cách lịch thiệp! / Politely refill the drink of the person on your left!"),
        PartyChallenge("dn_24", ChallengeType.RANDOM_TASKS, "Chọn một bài hát làm nhạc nền cho lượt tiếp theo! / Choose a background party soundtrack for the next round!"),
        PartyChallenge("dn_25", ChallengeType.RANDOM_TASKS, "Kể 1 câu chuyện vui chỉ bằng 5 từ duy nhất! / Tell a funny story using only 5 words!"),
        PartyChallenge("dn_26", ChallengeType.RANDOM_TASKS, "Miễn nhiễm mọi hình phạt trong 1 vòng chơi tiếp theo! / Immunity shield: Immune to penalties for next round!"),
        PartyChallenge("dn_27", ChallengeType.RANDOM_TASKS, "Nhường quyền chỉ định uống cho người ngồi đối diện! / Transfer penalty assigning power to the person opposite!"),
        PartyChallenge("dn_28", ChallengeType.RANDOM_TASKS, "Thử thách giữ thăng bằng chiếc nắp chai trên trán trong 10 giây! / Balance a bottle cap on your forehead for 10s!"),
        PartyChallenge("dn_29", ChallengeType.RANDOM_TASKS, "Đọc ngược bảng chữ cái từ Z về U thật nhanh! / Recite the alphabet backwards from Z to U!"),
        PartyChallenge("dn_30", ChallengeType.RANDOM_TASKS, "Hát một câu hát có từ 'say' hoặc 'rượu' hoặc 'party'! / Sing a lyric that contains 'drink', 'party' or 'night'!")
    )

    private val coupleFunChallenges = listOf(
        // Funny Questions
        PartyChallenge("cf_1", ChallengeType.FUNNY_QUESTIONS, "Ấn tượng đầu tiên của bạn về người ấy khi gặp mặt là gì? / What was your honest first impression?"),
        PartyChallenge("cf_2", ChallengeType.FUNNY_QUESTIONS, "Thói quen đáng yêu nhất và kỳ lạ nhất của đối phương? / Cutest and weirdest habit of your partner?"),
        PartyChallenge("cf_3", ChallengeType.FUNNY_QUESTIONS, "Chuyến đi đáng nhớ nhất mà hai bạn từng có cùng nhau? / Most memorable trip you two shared together?"),
        PartyChallenge("cf_4", ChallengeType.FUNNY_QUESTIONS, "Ai là người dỗi trước và ai thường làm lành trước? / Who gets mad first and who apologizes first?"),
        PartyChallenge("cf_5", ChallengeType.FUNNY_QUESTIONS, "Bài hát nào khiến bạn nhớ ngay đến người ấy? / What song immediately reminds you of them?"),
        PartyChallenge("cf_6", ChallengeType.FUNNY_QUESTIONS, "Món ăn nào người ấy nấu khiến bạn thích nhất? / Favorite dish cooked by your partner?"),

        // Quick Dares
        PartyChallenge("cf_7", ChallengeType.QUICK_DARES, "Nhìn vào mắt nhau không chớp mắt trong 30 giây! / Stare into each other's eyes for 30s without blinking!"),
        PartyChallenge("cf_8", ChallengeType.QUICK_DARES, "Nói 3 điều bạn yêu thích nhất ở người ấy ngay lúc này! / Say 3 things you love most about them right now!"),
        PartyChallenge("cf_9", ChallengeType.QUICK_DARES, "Tặng đối phương một cái ôm ấm áp trong 15 giây! / Give your partner a warm 15-second hug!"),
        PartyChallenge("cf_10", ChallengeType.QUICK_DARES, "Bắt chước một cử chỉ quen thuộc cực đáng yêu của người ấy! / Imitate a cute gesture of your partner!"),
        PartyChallenge("cf_11", ChallengeType.QUICK_DARES, "Hát hoặc đọc một đoạn thơ tình thật ngọt ngào! / Recite a sweet love line or song lyric!"),
        PartyChallenge("cf_12", ChallengeType.QUICK_DARES, "Đổi điện thoại cho nhau xem tấm ảnh chụp gần nhất! / Show each other the latest photo in your gallery!"),

        // Group Actions
        PartyChallenge("cf_13", ChallengeType.GROUP_ACTIONS, "Cùng đồng thanh nói ngày kỷ niệm của hai bạn cùng lúc! / Say your anniversary date together at the count of 3!"),
        PartyChallenge("cf_14", ChallengeType.GROUP_ACTIONS, "Cùng làm biểu tượng trái tim bằng hai bàn tay và giữ 10 giây! / Form a heart together with your hands for 10s!"),
        PartyChallenge("cf_15", ChallengeType.GROUP_ACTIONS, "Chơi oẳn tù tì 3 ván, người thua phải phục vụ người thắng đồ uống! / Rock paper scissors best of 3, loser serves drink!"),
        PartyChallenge("cf_16", ChallengeType.GROUP_ACTIONS, "Cùng chụp một bức ảnh selfie nhí nhố nhất kỷ niệm hôm nay! / Take a goofy selfie together!"),
        PartyChallenge("cf_17", ChallengeType.GROUP_ACTIONS, "Đoán món đồ người ấy thích nhất đang mang trên người! / Guess the item your partner likes best on them right now!"),
        PartyChallenge("cf_18", ChallengeType.GROUP_ACTIONS, "Cùng nhắm mắt và chỉ tay xem ai tiêu tiền nhiều hơn! / Close eyes and point to who spends more money!"),

        // Random Tasks
        PartyChallenge("cf_19", ChallengeType.RANDOM_TASKS, "Massage vai cho đối phương thật thoải mái trong 20 giây! / Give a 20-second relaxing shoulder massage!"),
        PartyChallenge("cf_20", ChallengeType.RANDOM_TASKS, "Lên kế hoạch hẹn hò cuối tuần lý tưởng trong vòng 1 phút! / Plan a dream weekend date in 1 minute!"),
        PartyChallenge("cf_21", ChallengeType.RANDOM_TASKS, "Viết một lời nhắn ngọt ngào vào ghi chú điện thoại của đối phương! / Leave a sweet note in their phone notes!"),
        PartyChallenge("cf_22", ChallengeType.RANDOM_TASKS, "Cùng khiêu vũ chậm 1 điệu nhảy ngẫu hứng! / Slow dance together for 20 seconds!"),
        PartyChallenge("cf_23", ChallengeType.RANDOM_TASKS, "Tặng người ấy một biệt danh mới cực dễ thương cho tối nay! / Give them a brand-new cute nickname for tonight!"),
        PartyChallenge("cf_24", ChallengeType.RANDOM_TASKS, "Hứa hẹn thực hiện 1 điều ước nhỏ của đối phương trong tuần này! / Grant 1 small wish for your partner this week!")
    )

    private val crazyDaresChallenges = listOf(
        // Funny Questions
        PartyChallenge("cd_1", ChallengeType.FUNNY_QUESTIONS, "Điều điên rồ nhất bạn từng dám làm trong đời là gì? / What is the wildest thing you ever did?"),
        PartyChallenge("cd_2", ChallengeType.FUNNY_QUESTIONS, "Nếu bị thách thức cạo trọc đầu lấy 5.000 USD, bạn có làm không? / Would you shave your head for $5,000?"),
        PartyChallenge("cd_3", ChallengeType.FUNNY_QUESTIONS, "Ai trong bàn này có vẻ bề ngoài hiền lành nhưng quậy ngầm nhất? / Who looks innocent but is secretly wild?"),
        PartyChallenge("cd_4", ChallengeType.FUNNY_QUESTIONS, "Nỗi sợ vô lý nhất mà bạn từng có là gì? / What is your most irrational fear?"),
        PartyChallenge("cd_5", ChallengeType.FUNNY_QUESTIONS, "Bạn từng trốn vé hay đi lạc vào một bữa tiệc lạ chưa? / Ever crashed a stranger's party?"),
        PartyChallenge("cd_6", ChallengeType.FUNNY_QUESTIONS, "Trải nghiệm rùng mình nhất bạn từng trải qua? / Spookiest experience you ever had?"),

        // Quick Dares
        PartyChallenge("cd_7", ChallengeType.QUICK_DARES, "Hít đất 10 cái liên tục ngay tại chỗ! / Drop and do 10 push-ups right now!"),
        PartyChallenge("cd_8", ChallengeType.QUICK_DARES, "Nói chuyện bằng giọng địa phương hoặc chất giọng nước ngoài trong 2 lượt! / Speak in a strange accent for 2 rounds!"),
        PartyChallenge("cd_9", ChallengeType.QUICK_DARES, "Để người bên phải vẽ một hình râu mèo lên má bằng bút dạ hoặc ngón tay! / Let the person on your right draw cat whiskers on your cheek!"),
        PartyChallenge("cd_10", ChallengeType.QUICK_DARES, "Tạo dáng siêu mẫu thời trang catwalk đi một vòng quanh bàn! / Runway catwalk fashion walk around the table!"),
        PartyChallenge("cd_11", ChallengeType.QUICK_DARES, "Đăng một story ngắn trên mạng xã hội chỉ với biểu tượng quả chuối! / Post a story with only a banana emoji!"),
        PartyChallenge("cd_12", ChallengeType.QUICK_DARES, "Thì thầm bí mật hài hước vào tai người ngồi bên trái! / Whisper a funny secret to the person on your left!"),

        // Group Actions
        PartyChallenge("cd_13", ChallengeType.GROUP_ACTIONS, "Cả nhóm cùng làm động tác sóng tay kiểu lễ hội âm nhạc! / Group wave like in a music festival!"),
        PartyChallenge("cd_14", ChallengeType.GROUP_ACTIONS, "Thử thách ma cà rồng: Cả bàn im lặng tuyệt đối trong 15 giây, ai cười bị phạt! / Vampire freeze: Total silence for 15s, first to laugh drinks!"),
        PartyChallenge("cd_15", ChallengeType.GROUP_ACTIONS, "Bầu chọn 'Vua tiệc tùng' của tối nay và tung hô người đó! / Vote the Party King/Queen and applaud them!"),
        PartyChallenge("cd_16", ChallengeType.GROUP_ACTIONS, "Cùng đập tay xoay tròn một vòng với tất cả mọi người! / High five everyone around the table in a circle!"),
        PartyChallenge("cd_17", ChallengeType.GROUP_ACTIONS, "Chơi trò kể chuyện nối đuôi: Mỗi người 1 câu chuyện kinh dị hài hước! / One-sentence horror comedy story around the table!"),
        PartyChallenge("cd_18", ChallengeType.GROUP_ACTIONS, "Ai chưa từng đi bar hoặc nhậu đêm thì nâng ly giao lưu! / Cheers with anyone who rarely parties!"),

        // Random Tasks
        PartyChallenge("cd_19", ChallengeType.RANDOM_TASKS, "Làm động tác plank trong 30 giây trong khi mọi người đếm ngược! / Plank for 30 seconds while everyone counts!"),
        PartyChallenge("cd_20", ChallengeType.RANDOM_TASKS, "Mô phỏng lại tiếng kêu của 3 con vật khác nhau cho bàn đoán! / Make sounds of 3 animals for people to guess!"),
        PartyChallenge("cd_21", ChallengeType.RANDOM_TASKS, "Ăn một miếng đồ ăn kèm ớt cay hoặc mù tạt (nếu có)! / Eat a spicy bite!"),
        PartyChallenge("cd_22", ChallengeType.RANDOM_TASKS, "Gọi điện cho một người bạn và hát chúc mừng sinh nhật dù không phải ngày sinh! / Call a friend and sing Happy Birthday!"),
        PartyChallenge("cd_23", ChallengeType.RANDOM_TASKS, "Cho phép người bên cạnh đổi ảnh đại diện mạng xã hội của bạn trong 1 giờ! / Let them pick your profile avatar for 1 hour!"),
        PartyChallenge("cd_24", ChallengeType.RANDOM_TASKS, "Đọc to một dòng tweet hoặc status ngẫu nhiên trên trang chủ mạng xã hội! / Read aloud a random social media post!"),
        PartyChallenge("cd_25", ChallengeType.RANDOM_TASKS, "Hô vang khẩu hiệu 'Tôi là người dũng cảm nhất hôm nay!' thật dõng dạc! / Shout 'I am the bravest person here!' proudly!")
    )

    private val icebreakerChallenges = listOf(
        // Funny Questions
        PartyChallenge("ib_1", ChallengeType.FUNNY_QUESTIONS, "Nếu bạn được sinh ra là một siêu anh hùng, năng lực của bạn sẽ là gì? / If you were a superhero, what power would you choose?"),
        PartyChallenge("ib_2", ChallengeType.FUNNY_QUESTIONS, "Món ăn yêu thích nhất mà bạn có thể ăn suốt cả tuần không ngán? / Favorite food you could eat all week?"),
        PartyChallenge("ib_3", ChallengeType.FUNNY_QUESTIONS, "Địa điểm du lịch mơ ước tiếp theo của bạn là ở đâu? / What is your dream travel destination?"),
        PartyChallenge("ib_4", ChallengeType.FUNNY_QUESTIONS, "Bộ phim hoặc bài hát gần đây khiến bạn say mê nhất? / Favorite movie or song you are obsessed with lately?"),
        PartyChallenge("ib_5", ChallengeType.FUNNY_QUESTIONS, "Nếu trúng số độc đắc ngày mai, việc đầu tiên bạn làm là gì? / First thing you do if you win the lottery?"),

        // Quick Dares
        PartyChallenge("ib_6", ChallengeType.QUICK_DARES, "Tự giới thiệu bản thân bằng 3 từ tính từ độc đáo nhất! / Introduce yourself with 3 unique adjectives!"),
        PartyChallenge("ib_7", ChallengeType.QUICK_DARES, "Bắt tay thật nồng nhiệt và nói một lời chúc với 2 người mới quen! / Enthusiastic handshake with 2 people!"),
        PartyChallenge("ib_8", ChallengeType.QUICK_DARES, "Kể tên 5 bài hát bạn thích nhất trong vòng 10 giây! / Name 5 favorite songs in 10 seconds!"),
        PartyChallenge("ib_9", ChallengeType.QUICK_DARES, "Khen một điểm nổi bật ở người ngồi đối diện! / Compliment a standout trait of the person opposite!"),
        PartyChallenge("ib_10", ChallengeType.QUICK_DARES, "Cười thật sảng khoái trong 5 giây như vừa nghe truyện cười đỉnh nhất! / Laugh heartily for 5 seconds!"),

        // Group Actions
        PartyChallenge("ib_11", ChallengeType.GROUP_ACTIONS, "Cả nhóm cùng đứng dậy và vươn vai thư giãn thật thoải mái! / Everyone stand up and stretch together!"),
        PartyChallenge("ib_12", ChallengeType.GROUP_ACTIONS, "Mỗi người chia sẻ tên một sở thích kỳ lạ của mình! / Everyone shares one quirky hobby!"),
        PartyChallenge("ib_13", ChallengeType.GROUP_ACTIONS, "Trò chơi 'Hai thật một giả': 1 người nói 3 điều, nhóm đoán điều nào giả! / Two truths and a lie!"),
        PartyChallenge("ib_14", ChallengeType.GROUP_ACTIONS, "Cả bàn cùng vỗ tay theo nhịp một điệu nhạc sôi động! / Clap to the rhythm together!"),
        PartyChallenge("ib_15", ChallengeType.GROUP_ACTIONS, "Điểm danh cung hoàng đạo của tất cả mọi người trong phòng! / Check zodiac signs around the table!"),

        // Random Tasks
        PartyChallenge("ib_16", ChallengeType.RANDOM_TASKS, "Kể một sự thật thú vị mà bạn nghĩ ít người biết! / Share a fun fact you think few people know!"),
        PartyChallenge("ib_17", ChallengeType.RANDOM_TASKS, "Thể hiện một tài lẻ bất kỳ của bạn (uốn dẻo, huýt sáo, búng tay...)! / Show a random hidden talent!"),
        PartyChallenge("ib_18", ChallengeType.RANDOM_TASKS, "Diễn xuất biểu cảm khuôn mặt từ vui sang buồn rồi bất ngờ trong 5 giây! / Show 3 emotions in 5 seconds!"),
        PartyChallenge("ib_19", ChallengeType.RANDOM_TASKS, "Chỉ định người tiếp theo gieo xúc xắc và trao quyền ưu tiên! / Nominate the next roller and grant them priority!"),
        PartyChallenge("ib_20", ChallengeType.RANDOM_TASKS, "Chúc một lời chúc may mắn và thành công tới cả nhóm! / Give a hearty wish of luck and joy to the group!")
    )

    private val packs = listOf(
        PartyChallengePack(
            id = "classic_party",
            name = "Classic Party",
            description = "Roll the dice and complete fun party challenges with your friends.",
            challengesCount = 30,
            playersRange = "2–8",
            difficulty = "Easy",
            isFeatured = true,
            tags = listOf("Party", "2–8", "Easy"),
            iconRes = R.drawable.ic_dice_d6,
            challenges = classicPartyChallenges
        ),
        PartyChallengePack(
            id = "drinking_night",
            name = "Drinking Night",
            description = "Perfect for parties and gatherings. Sip, laugh, and celebrate together.",
            challengesCount = 30,
            playersRange = "2–4",
            difficulty = "Easy",
            isFeatured = false,
            tags = listOf("Party", "2–4", "Easy"),
            iconRes = R.drawable.ic_trophy,
            challenges = drinkingNightChallenges
        ),
        PartyChallengePack(
            id = "couple_fun",
            name = "Couple Night",
            description = "Romantic, sweet, and hilarious questions for couples and close friends.",
            challengesCount = 24,
            playersRange = "2",
            difficulty = "Medium",
            isFeatured = false,
            tags = listOf("Couple", "2", "Medium"),
            iconRes = R.drawable.ic_star_rate,
            challenges = coupleFunChallenges
        ),
        PartyChallengePack(
            id = "crazy_dares",
            name = "Crazy Dares",
            description = "Wild challenges and daring tasks for adventurous party players.",
            challengesCount = 25,
            playersRange = "3–6",
            difficulty = "Hard",
            isFeatured = false,
            tags = listOf("Dares", "3–6", "Hard"),
            iconRes = R.drawable.ic_type_dare,
            challenges = crazyDaresChallenges
        ),
        PartyChallengePack(
            id = "icebreaker",
            name = "Icebreaker",
            description = "Quick icebreakers and fun questions to warm up any gathering.",
            challengesCount = 20,
            playersRange = "2–10",
            difficulty = "Easy",
            isFeatured = false,
            tags = listOf("Icebreaker", "2–10", "Easy"),
            iconRes = R.drawable.ic_type_funny,
            challenges = icebreakerChallenges
        )
    )

    fun getAllPacks(): List<PartyChallengePack> = packs

    fun getFeaturedPack(): PartyChallengePack = packs.first { it.isFeatured }

    fun getRecentPacks(): List<PartyChallengePack> = packs

    fun getRecommendedPacks(): List<PartyChallengePack> = packs.filter { !it.isFeatured }

    fun getPackById(id: String): PartyChallengePack {
        return packs.find { it.id == id } ?: getFeaturedPack()
    }

    fun getRandomChallenge(packId: String): PartyChallenge {
        val pack = getPackById(packId)
        return pack.challenges.random()
    }
}
