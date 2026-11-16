fn main() {
    #[cfg(feature = "foo")]
    println!("{}", lib::answer());
}

#[cfg(test)]
mod tests {
    #[test]
    fn answer() {
        assert_eq!(lib::answer(), 42);
    }
}
