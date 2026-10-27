package com.phuongthanh.diceroller.features.party.model

enum class QuestionType {
    TRUTH,
    DARE
}

data class PartyQuestion(
    val id: String,
    val type: QuestionType,
    val text: String
) {
    constructor(intId: Int, type: QuestionType, text: String) : this(intId.toString(), type, text)
}

object PartyQuestionsBank {
    val questions = listOf(
        // TRUTHS
        PartyQuestion(1, QuestionType.TRUTH, "Điều ngốc nghếch nhất bạn từng làm khi say là gì? / What's the silliest thing you did while drunk?"),
        PartyQuestion(2, QuestionType.TRUTH, "Ai là crush đầu tiên của bạn? / Who was your very first crush?"),
        PartyQuestion(3, QuestionType.TRUTH, "Bí mật lớn nhất bạn chưa từng kể với ai trong nhóm? / What is your biggest secret?"),
        PartyQuestion(4, QuestionType.TRUTH, "Tin nhắn ngượng ngùng nhất gần đây trong điện thoại bạn? / What is the most embarrassing text in your phone?"),
        PartyQuestion(5, QuestionType.TRUTH, "Bạn có thói quen kỳ quặc nào khi ở một mình? / What weird habit do you have when alone?"),
        PartyQuestion(6, QuestionType.TRUTH, "Ai trong bàn này bạn cảm thấy đáng tin cậy nhất? / Who at this table do you trust the most?"),
        PartyQuestion(7, QuestionType.TRUTH, "Lần gần nhất bạn nói dối là khi nào và vì lý do gì? / When was the last time you lied and why?"),
        PartyQuestion(8, QuestionType.TRUTH, "Nếu được đổi cuộc sống với 1 người trong nhóm 1 ngày, bạn chọn ai? / Who would you swap lives with for a day?"),
        PartyQuestion(9, QuestionType.TRUTH, "Nỗi sợ kỳ lạ nhất của bạn là gì? / What is your weirdest fear?"),
        PartyQuestion(10, QuestionType.TRUTH, "Khoảnh khắc xấu hổ nhất thời đi học của bạn? / What was your most embarrassing school moment?"),
        PartyQuestion(11, QuestionType.TRUTH, "Bạn từng thích thầm người yêu cũ của bạn thân chưa? / Have you ever had a crush on a friend's ex?"),
        PartyQuestion(12, QuestionType.TRUTH, "Nếu ngày mai tận thế, bạn sẽ nhắn tin cho ai đầu tiên? / If the world ends tomorrow, who do you text first?"),

        // DARES
        PartyQuestion(13, QuestionType.DARE, "Hát điệp khúc một bài hát bằng giọng sóc chuột! / Sing a chorus in a chipmunk voice!"),
        PartyQuestion(14, QuestionType.DARE, "Uống 1 ly không dùng tay! / Drink 1 glass without using hands!"),
        PartyQuestion(15, QuestionType.DARE, "Gọi điện cho người thứ 3 trong danh bạ và nói 'Tôi nhớ bạn'. / Call the 3rd person in contacts and say 'I miss you'."),
        PartyQuestion(16, QuestionType.DARE, "Bắt chước dáng đi của một người trong bàn và để mọi người đoán. / Imitate someone's walk at the table."),
        PartyQuestion(17, QuestionType.DARE, "Gửi tin nhắn 'Em đói bụng quá' cho một người bạn bất kỳ. / Send 'I am so hungry' to a random friend."),
        PartyQuestion(18, QuestionType.DARE, "Làm mặt xấu nhất có thể và giữ yên trong 10 giây cho mọi người chụp hình. / Make your ugliest face for 10s."),
        PartyQuestion(19, QuestionType.DARE, "Đọc to tin nhắn gần nhất bạn nhận được. / Read aloud the latest message you received."),
        PartyQuestion(20, QuestionType.DARE, "Nhảy một điệu freestyle trong 20 giây! / Do a 20-second freestyle dance!"),
        PartyQuestion(21, QuestionType.DARE, "Uống cạn nửa ly cùng người ngồi bên tay phải! / Drink half a glass with the person on your right!"),
        PartyQuestion(22, QuestionType.DARE, "Nói giọng người máy trong 2 lượt tiếp theo. / Speak in a robot voice for the next 2 turns.")
    )

    fun getRandomQuestion(type: QuestionType): PartyQuestion {
        val filtered = questions.filter { it.type == type }
        return filtered.random()
    }
}
